//
//  YdkPermissionServiceCamera.m
//  ydk-permission
//
//  Created by yryz on 2019/7/11.
//

#import "YdkPermissionServiceCamera.h"
#import <AVKit/AVKit.h>

@implementation YdkPermissionServiceCamera

// 1. 获取权限状态
- (YdkPermissionAuthorizationStatus)permissionAuthorizationStatus {
    AVAuthorizationStatus status = [AVCaptureDevice authorizationStatusForMediaType:AVMediaTypeVideo];
    return [self permissionStatus:status];
}

- (YdkPermissionAuthorizationStatus)permissionStatus:(AVAuthorizationStatus)status {
    switch (status) {
        case AVAuthorizationStatusNotDetermined:
            return YdkPermissionAuthorizationStatusNotDetermined;
            break;
        case AVAuthorizationStatusRestricted:
            return YdkPermissionAuthorizationStatusRestricted;
            break;
        case AVAuthorizationStatusDenied:
            return YdkPermissionAuthorizationStatusDenied;
            break;
        case AVAuthorizationStatusAuthorized:
            return YdkPermissionAuthorizationStatusAuthorized;
            break;
    }
}

// 2. 请求权限并返回权限状态
- (RACSignal<NSNumber/*PermissionAuthorizationStatus*/ *> *)requestAuthorization {
    return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
        [AVCaptureDevice requestAccessForMediaType:AVMediaTypeVideo completionHandler:^(BOOL granted) {
            [subscriber sendNext:@(granted ? YdkPermissionAuthorizationStatusAuthorized : YdkPermissionAuthorizationStatusDenied)];
            [subscriber sendCompleted];
        }];
        return nil;
    }];
}

@end
