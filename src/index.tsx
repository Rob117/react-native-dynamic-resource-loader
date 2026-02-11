import DynamicResourceLoader from './NativeDynamicResourceLoader';

export function checkResourcesAvailable(
  tags: ReadonlyArray<string>
): Promise<boolean> {
  return DynamicResourceLoader.checkResourcesAvailable(tags);
}

export function downloadResources(
  tags: ReadonlyArray<string>
): Promise<boolean> {
  return DynamicResourceLoader.downloadResources(tags);
}

export function endAccessingResources(tags: ReadonlyArray<string>): void {
  DynamicResourceLoader.endAccessingResources(tags);
}

export function getResourcePath(
  resourceName: string,
  ofType: string
): Promise<string> {
  return DynamicResourceLoader.getResourcePath(resourceName, ofType);
}

export function setPreservationPriority(
  priority: number,
  tags: ReadonlyArray<string>
): void {
  DynamicResourceLoader.setPreservationPriority(priority, tags);
}
