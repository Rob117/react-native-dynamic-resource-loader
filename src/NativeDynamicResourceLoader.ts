import { TurboModuleRegistry, type TurboModule } from 'react-native';

export interface Spec extends TurboModule {
  checkResourcesAvailable(tags: ReadonlyArray<string>): Promise<boolean>;
  downloadResources(tags: ReadonlyArray<string>): Promise<boolean>;
  endAccessingResources(tags: ReadonlyArray<string>): void;
  getResourcePath(resourceName: string, ofType: string): Promise<string>;
  setPreservationPriority(priority: number, tags: ReadonlyArray<string>): void;
}

export default TurboModuleRegistry.getEnforcing<Spec>('DynamicResourceLoader');
