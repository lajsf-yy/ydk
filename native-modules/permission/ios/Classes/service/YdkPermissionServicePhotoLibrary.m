//
//  YdkPermissionServicePhotoLibrary.m
//  ydk-permission
//
//  Created by yryz on 2019/7/11.
//

#import "YdkPermissionServicePhotoLibrary.h"
#import <Photos/Photos.h>

@implementation YdkPermissionServicePhotoLibrary

// 1. 获取权限状态
- (YdkPermissionAuthorizationStatus)permissionAuthorizationStatus {
    PHAuthorizationStatus status = [PHPhotoLibrary authorizationStatus];
    return [self permissionStatus:status];
}

- (YdkPermissionAuthorizationStatus)permissionStatus:(PHAuthorizationStatus)status {
    switch (status) {
        case PHAuthorizationStatusNotDetermined:
            return YdkPermissionAuthorizationStatusNotDetermined;
            break;
        case PHAuthorizationStatusRestricted:
            return YdkPermissionAuthorizationStatusRestricted;
            break;
        case PHAuthorizationStatusDenied:
            return YdkPermissionAuthorizationStatusDenied;
            break;
        case PHAuthorizationStatusAuthorized:
            return YdkPermissionAuthorizationStatusAuthorized;
            break;
    }
}

// 2. 请求权限并返回权限状态
- (RACSignal<NSNumber/*YdkPermissionAuthorizationStatus*/ *> *)requestAuthorization {
    return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
        [PHPhotoLibrary requestAuthorization:^(PHAuthorizationStatus status) {
            [subscriber sendNext:@([self permissionStatus:status])];
            [subscriber sendCompleted];
        }];
        return nil;
    }];
}

@end
