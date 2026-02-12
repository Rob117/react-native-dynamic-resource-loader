package com.dynamicresourceloader

import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.google.android.play.core.assetpacks.AssetPackManager
import com.google.android.play.core.assetpacks.AssetPackManagerFactory
import com.google.android.play.core.assetpacks.AssetPackState
import com.google.android.play.core.assetpacks.AssetPackStateUpdateListener
import com.google.android.play.core.assetpacks.AssetPackStates
import com.google.android.play.core.assetpacks.model.AssetPackStatus
import java.io.File

class DynamicResourceLoaderModule(reactContext: ReactApplicationContext) :
  NativeDynamicResourceLoaderSpec(reactContext) {

  private val assetPackManager: AssetPackManager =
    AssetPackManagerFactory.getInstance(reactContext)

  private fun emitProgress(tag: String, bytesDownloaded: Long, totalBytes: Long, fraction: Double, status: String) {
    emitOnDownloadProgress(Arguments.createMap().apply {
      putString("tag", tag)
      putDouble("bytesDownloaded", bytesDownloaded.toDouble())
      putDouble("totalBytes", totalBytes.toDouble())
      putDouble("fractionCompleted", fraction)
      putString("status", status)
    })
  }

  private fun ReadableArray.toStringList(): List<String> {
    val list = mutableListOf<String>()
    for (i in 0 until size()) {
      getString(i)?.let { list.add(it) }
    }
    return list
  }

  override fun checkResourcesAvailable(tags: ReadableArray, promise: Promise) {
    val packNames = tags.toStringList()
    assetPackManager.getPackStates(packNames)
      .addOnSuccessListener { states: AssetPackStates ->
        val allCompleted = packNames.all { name ->
          states.packStates()[name]?.status() == AssetPackStatus.COMPLETED
        }
        promise.resolve(allCompleted)
      }
      .addOnFailureListener { e ->
        promise.reject("ERR_CHECK_RESOURCES", e.message, e)
      }
  }

  override fun downloadResources(tags: ReadableArray, promise: Promise) {
    val packNames = tags.toStringList()

    assetPackManager.getPackStates(packNames)
      .addOnSuccessListener { states: AssetPackStates ->
        val allCompleted = packNames.all { name ->
          states.packStates()[name]?.status() == AssetPackStatus.COMPLETED
        }
        if (allCompleted) {
          for (name in packNames) {
            emitProgress(name, 1, 1, 1.0, "completed")
          }
          promise.resolve(true)
          return@addOnSuccessListener
        }

        val pending = packNames.filter { name ->
          states.packStates()[name]?.status() != AssetPackStatus.COMPLETED
        }.toMutableSet()
        var settled = false

        val listener = object : AssetPackStateUpdateListener {
          override fun onStateUpdate(state: AssetPackState) {
            if (settled) return

            val name = state.name()
            val bytesDownloaded = state.bytesDownloaded()
            val totalBytes = state.totalBytesToDownload()
            val fraction = if (totalBytes > 0) bytesDownloaded.toDouble() / totalBytes.toDouble() else 0.0

            when (state.status()) {
              AssetPackStatus.DOWNLOADING, AssetPackStatus.TRANSFERRING -> {
                emitProgress(name, bytesDownloaded, totalBytes, fraction, "downloading")
              }
              AssetPackStatus.WAITING_FOR_WIFI -> {
                emitProgress(name, bytesDownloaded, totalBytes, fraction, "waiting")
              }
              AssetPackStatus.COMPLETED -> {
                emitProgress(name, totalBytes, totalBytes, 1.0, "completed")
                pending.remove(name)
                if (pending.isEmpty()) {
                  settled = true
                  assetPackManager.unregisterListener(this)
                  promise.resolve(true)
                }
              }
              AssetPackStatus.FAILED -> {
                emitProgress(name, bytesDownloaded, totalBytes, fraction, "failed")
                settled = true
                assetPackManager.unregisterListener(this)
                promise.reject(
                  "ERR_DOWNLOAD_FAILED",
                  "Asset pack '$name' failed with error code ${state.errorCode()}"
                )
              }
              AssetPackStatus.REQUIRES_USER_CONFIRMATION -> {
                settled = true
                assetPackManager.unregisterListener(this)
                promise.reject(
                  "ERR_REQUIRES_USER_CONFIRMATION",
                  "Asset pack '$name' requires user confirmation"
                )
              }
            }
          }
        }

        assetPackManager.registerListener(listener)
        assetPackManager.fetch(packNames)
      }
      .addOnFailureListener { e ->
        promise.reject("ERR_DOWNLOAD_RESOURCES", e.message, e)
      }
  }

  override fun endAccessingResources(tags: ReadableArray) {
    val packNames = tags.toStringList()
    for (name in packNames) {
      assetPackManager.removePack(name)
    }
  }

  override fun getResourcePath(resourceName: String, ofType: String, promise: Promise) {
    val locations = assetPackManager.packLocations
    for ((_, location) in locations) {
      val file = File(location.assetsPath(), "$resourceName.$ofType")
      if (file.exists()) {
        promise.resolve(file.absolutePath)
        return
      }
    }
    promise.reject("RESOURCE_NOT_FOUND", "Could not find $resourceName.$ofType in any asset pack")
  }

  override fun setPreservationPriority(priority: Double, tags: ReadableArray) {
    // No-op on Android — Play Asset Delivery has no preservation priority equivalent
  }

  companion object {
    const val NAME = NativeDynamicResourceLoaderSpec.NAME
  }
}
