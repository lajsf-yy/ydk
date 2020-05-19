
#import "YdkShareModule.h"
#import "YdkShare.h"

@implementation YdkMobShareModule

RCT_EXPORT_MODULE(YdkShareModule)

+ (BOOL)requiresMainQueueSetup {
    return true;
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(share:(NSString *)platform shareContent:(NSDictionary *)shareDict resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    YdkShareData* shareData = [shareDict toObject:[YdkShareData class]];
    [YdkShare share:platform shareData:shareData resolve:resolver reject:rejecter];
}

RCT_EXPORT_METHOD(authorizeLogin:(NSString *)platform resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    [YdkShare authorizeLogin:platform resolve:resolver reject:rejecter];
}

RCT_EXPORT_METHOD(getInstallPlatforms:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    [YdkShare getInstallPlatforms:resolver reject:rejecter];
}

RCT_EXPORT_METHOD(authorize:(NSString *)platform resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    [YdkShare authorize:platform resolve:resolver reject:rejecter];
}

@end


