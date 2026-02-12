import Foundation

@objcMembers
public class DynamicResourceLoaderImpl: NSObject {
  private var activeRequests: [String: NSBundleResourceRequest] = [:]
  private var progressObservations: [String: NSKeyValueObservation] = [:]

  private func requestKey(for tags: [String]) -> String {
    return tags.sorted().joined(separator: ",")
  }

  public func checkResourcesAvailable(
    _ tags: [String],
    resolve: @escaping (Bool) -> Void,
    reject: @escaping (String, String, NSError) -> Void
  ) {
    let tagSet = Set(tags)
    let request = NSBundleResourceRequest(tags: tagSet)

    request.conditionallyBeginAccessingResources { available in
      if available {
        request.endAccessingResources()
      }
      resolve(available)
    }
  }

  public func downloadResources(
    _ tags: [String],
    progress progressCallback: ((String, Int64, Int64, Double, String) -> Void)?,
    resolve: @escaping (Bool) -> Void,
    reject: @escaping (String, String, NSError) -> Void
  ) {
    let key = requestKey(for: tags)
    let tagSet = Set(tags)
    let request = NSBundleResourceRequest(tags: tagSet)

    request.conditionallyBeginAccessingResources { [weak self] available in
      if available {
        self?.activeRequests[key] = request
        for tag in tags {
          progressCallback?(tag, 1, 1, 1.0, "completed")
        }
        resolve(true)
        return
      }

      if let progressCallback = progressCallback {
        let observation = request.progress.observe(\.fractionCompleted) { progress, _ in
          for tag in tags {
            progressCallback(
              tag,
              progress.completedUnitCount,
              progress.totalUnitCount,
              progress.fractionCompleted,
              "downloading"
            )
          }
        }
        self?.progressObservations[key] = observation
      }

      request.beginAccessingResources { [weak self] error in
        self?.progressObservations.removeValue(forKey: key)
        if let error = error as NSError? {
          for tag in tags {
            progressCallback?(tag, 0, 0, 0.0, "failed")
          }
          reject("DOWNLOAD_FAILED", error.localizedDescription, error)
          return
        }
        self?.activeRequests[key] = request
        for tag in tags {
          progressCallback?(tag, 1, 1, 1.0, "completed")
        }
        resolve(true)
      }
    }
  }

  public func endAccessingResources(_ tags: [String]) {
    let key = requestKey(for: tags)
    guard let request = activeRequests[key] else { return }
    request.endAccessingResources()
    activeRequests.removeValue(forKey: key)
  }

  public func getResourcePath(
    _ resourceName: String,
    ofType type: String,
    resolve: @escaping (String) -> Void,
    reject: @escaping (String, String, NSError) -> Void
  ) {
    if let path = Bundle.main.path(forResource: resourceName, ofType: type) {
      resolve(path)
    } else {
      let error = NSError(
        domain: "DynamicResourceLoader",
        code: 1,
        userInfo: [NSLocalizedDescriptionKey: "Resource '\(resourceName).\(type)' not found"]
      )
      reject("RESOURCE_NOT_FOUND", error.localizedDescription, error)
    }
  }

  public func setPreservationPriority(_ priority: Double, forTags tags: [String]) {
    let tagSet = Set(tags)
    Bundle.main.setPreservationPriority(priority, forTags: tagSet)
  }
}
