#import "YdkCacheModule.h"

@implementation YdkCacheModule{
  YdkCache *_cache;
}

- (instancetype)init {
  self = [super init];
  if (self) {
    _cache = ydk_get_module_instance([YdkCache class]);
  }
  return self;
}

RCT_EXPORT_MODULE()
+ (BOOL)requiresMainQueueSetup{
    return true;
}
RCT_EXPORT_METHOD(getCacheSize:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
  NSUInteger totalSize = [_cache getCacheSize];
	NSDictionary *dict = @{@"cacheSize":@(totalSize)};
  resolver(dict);
}

RCT_EXPORT_METHOD(clearCache:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
  [_cache clearCache];
	  resolver(nil);
}

@end
