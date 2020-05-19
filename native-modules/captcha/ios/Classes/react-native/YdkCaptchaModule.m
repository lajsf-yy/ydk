//
//  YdkCaptchaModule.m
//  ydk-captcha
//
//  Created by yryz on 2019/7/12.
//

#import "YdkCaptchaModule.h"
#import "YdkCaptcha.h"

@implementation YdkCaptchaModule

RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup {
    return NO;
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(start:(NSString *)phone resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    YdkCaptcha *captcha= ydk_get_module_instance(YdkCaptcha.class);
    [[captcha start:phone] subscribeNext:^(id x) {
        resolver(nil);
    } error:^(NSError *error) {
        rejecter(@(error.code).stringValue, [error.userInfo objectForKey:NSLocalizedDescriptionKey] ? : @"验证失败", error);
    }];
}

RCT_EXPORT_METHOD(stop) {
    YdkCaptcha *captcha= ydk_get_module_instance(YdkCaptcha.class);
    [captcha stop];
}

@end
