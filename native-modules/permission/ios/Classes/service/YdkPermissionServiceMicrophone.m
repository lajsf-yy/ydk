//
//  YdkPermissionServiceMicrophone.m
//  ydk-permission
//
//  Created by yryz on 2019/7/11.
//

#import "YdkPermissionServiceMicrophone.h"
#import <AVKit/AVKit.h>

@implementation YdkPermissionServiceMicrophone

// 1. 获取权限状态
- (YdkPermissionAuthorizationStatus)permissionAuthorizationStatus {
    AVAudioSessionRecordPermission status = [AVAudioSession sharedInstance].recordPermission;
    return [self permissionStatus:status];
}

- (YdkPermissionAuthorizationStatus)permissionStatus:(AVAudioSessionRecordPermission)status {
    switch (status) {
        case AVAudioSessionRecordPermissionUndetermined:
            return YdkPermissionAuthorizationStatusNotDetermined;
            break;
        case AVAudioSessionRecordPermissionDenied:
            return YdkPermissionAuthorizationStatusDenied;
            break;
        case AVAudioSessionRecordPermissionGranted:
            return YdkPermissionAuthorizationStatusAuthorized;
            break;
    }
}

// 2. 请求权限并返回权限状态
- (RACSignal<NSNumber/*YdkPermissionAuthorizationStatus*/ *> *)requestAuthorization {
    return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
        [[AVAudioSession sharedInstance] requestRecordPermission:^(BOOL granted) {
            [subscriber sendNext:@(granted ? YdkPermissionAuthorizationStatusAuthorized : YdkPermissionAuthorizationStatusDenied)];
            [subscriber sendCompleted];
        }];
        return nil;
    }];
}

@end


