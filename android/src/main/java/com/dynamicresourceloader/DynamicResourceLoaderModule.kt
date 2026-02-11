package com.dynamicresourceloader

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray

class DynamicResourceLoaderModule(reactContext: ReactApplicationContext) :
  NativeDynamicResourceLoaderSpec(reactContext) {

  override fun checkResourcesAvailable(tags: ReadableArray, promise: Promise) {
    promise.reject("ERR_NOT_IMPLEMENTED", "On-Demand Resources are not available on Android")
  }

  override fun downloadResources(tags: ReadableArray, promise: Promise) {
    promise.reject("ERR_NOT_IMPLEMENTED", "On-Demand Resources are not available on Android")
  }

  override fun endAccessingResources(tags: ReadableArray) {
    // No-op on Android
  }

  override fun getResourcePath(resourceName: String, ofType: String, promise: Promise) {
    promise.reject("ERR_NOT_IMPLEMENTED", "On-Demand Resources are not available on Android")
  }

  override fun setPreservationPriority(priority: Double, tags: ReadableArray) {
    // No-op on Android
  }

  companion object {
    const val NAME = NativeDynamicResourceLoaderSpec.NAME
  }
}
