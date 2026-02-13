#import "DynamicResourceLoader.h"
#import <React/RCTBridge+Private.h>
#if __has_include("DynamicResourceLoader-Swift.h")
  #import "DynamicResourceLoader-Swift.h"
#else
  #import <DynamicResourceLoader/DynamicResourceLoader-Swift.h>
#endif

@implementation DynamicResourceLoader {
  DynamicResourceLoaderImpl *_impl;
}

RCT_EXPORT_MODULE(DynamicResourceLoader)

- (instancetype)init {
  self = [super init];
  if (self) {
    _impl = [[DynamicResourceLoaderImpl alloc] init];
  }
  return self;
}

RCT_EXPORT_METHOD(checkResourcesAvailable:(NSArray<NSString *> *)tags
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
  [_impl checkResourcesAvailable:tags
                         resolve:^(BOOL available) {
    resolve(@(available));
  }
                          reject:^(NSString *code, NSString *message, NSError *error) {
    reject(code, message, error);
  }];
}

RCT_EXPORT_METHOD(downloadResources:(NSArray<NSString *> *)tags
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
  [_impl downloadResources:tags
                  progress:^(NSString *tag, int64_t bytesDownloaded, int64_t totalBytes, double fractionCompleted, NSString *status) {
    [self emitOnDownloadProgress:@{
      @"tag": tag,
      @"bytesDownloaded": @(bytesDownloaded),
      @"totalBytes": @(totalBytes),
      @"fractionCompleted": @(fractionCompleted),
      @"status": status,
    }];
  }
                   resolve:^(BOOL success) {
    resolve(@(success));
  }
                    reject:^(NSString *code, NSString *message, NSError *error) {
    reject(code, message, error);
  }];
}

RCT_EXPORT_METHOD(endAccessingResources:(NSArray<NSString *> *)tags)
{
  [_impl endAccessingResources:tags];
}

RCT_EXPORT_METHOD(getResourcePath:(NSString *)resourceName
                  ofType:(NSString *)ofType
                  resolve:(RCTPromiseResolveBlock)resolve
                  reject:(RCTPromiseRejectBlock)reject)
{
  [_impl getResourcePath:resourceName
                  ofType:ofType
                 resolve:^(NSString *path) {
    resolve(path);
  }
                  reject:^(NSString *code, NSString *message, NSError *error) {
    reject(code, message, error);
  }];
}

RCT_EXPORT_METHOD(setPreservationPriority:(double)priority
                  tags:(NSArray<NSString *> *)tags)
{
  [_impl setPreservationPriority:priority forTags:tags];
}

- (void)emitOnDownloadProgress:(NSDictionary *)value {
  if (_eventEmitterCallback) {
    [super emitOnDownloadProgress:value];
  } else {
    [[RCTBridge currentBridge] enqueueJSCall:@"RCTDeviceEventEmitter"
                                      method:@"emit"
                                        args:@[@"onDownloadProgress", value]
                                  completion:NULL];
  }
}

- (std::shared_ptr<facebook::react::TurboModule>)getTurboModule:
    (const facebook::react::ObjCTurboModule::InitParams &)params
{
    return std::make_shared<facebook::react::NativeDynamicResourceLoaderSpecJSI>(params);
}

@end
