import {
  TurboModuleRegistry,
  NativeModules,
  type TurboModule,
} from 'react-native';
import type { EventEmitter } from 'react-native/Libraries/Types/CodegenTypes';

export type DownloadProgressEvent = {
  tag: string;
  bytesDownloaded: number;
  totalBytes: number;
  fractionCompleted: number;
  status: string;
};

export interface Spec extends TurboModule {
  checkResourcesAvailable(tags: ReadonlyArray<string>): Promise<boolean>;
  downloadResources(tags: ReadonlyArray<string>): Promise<boolean>;
  endAccessingResources(tags: ReadonlyArray<string>): void;
  getResourcePath(resourceName: string, ofType: string): Promise<string>;
  setPreservationPriority(priority: number, tags: ReadonlyArray<string>): void;
  readonly onDownloadProgress: EventEmitter<DownloadProgressEvent>;
}

export default (TurboModuleRegistry.get<Spec>('DynamicResourceLoader') ??
  NativeModules.DynamicResourceLoader) as Spec;
