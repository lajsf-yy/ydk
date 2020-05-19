//
//  YdkPermissionServiceNotification.m
//  ydk-permission
//
//  Created by yryz on 2019/7/11.
//

#import "YdkPermissionServiceNotification.h"
#import <UserNotifications/UserNotifications.h>

@implementation YdkPermissionServiceNotification

// 1. 获取权限状态
- (YdkPermissionAuthorizationStatus)permissionAuthorizationStatus {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
    if (UIApplication.sharedApplication.currentUserNotificationSettings.types != 0) {
#pragma clang diagnostic pop
        return YdkPermissionAuthorizationStatusAuthorized;
    } else {
        return YdkPermissionAuthorizationStatusDenied;
    }
}

// 2. 请求权限并返回权限状态
- (RACSignal<NSNumber/*YdkPermissionAuthorizationStatus*/ *> *)requestAuthorization {
    if (@available(iOS 10.0, *)) {
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            UNUserNotificationCenter *center = [UNUserNotificationCenter currentNotificationCenter];
            [center requestAuthorizationWithOptions:UNAuthorizationOptionBadge | UNAuthorizationOptionSound | UNAuthorizationOptionAlert completionHandler:^(BOOL granted, NSError *error) {
                if (error) {
                    [subscriber sendError:error];
                } else {
                    [subscriber sendNext:@(granted ? YdkPermissionAuthorizationStatusAuthorized : YdkPermissionAuthorizationStatusDenied)];
                    [subscriber sendCompleted];
                }
            }];
            return nil;
        }];
    } else {
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            UIUserNotificationSettings *settings = [UIUserNotificationSettings settingsForTypes:UIUserNotificationTypeBadge | UIUserNotificationTypeSound | UIUserNotificationTypeAlert categories:nil];
            [UIApplication.sharedApplication registerUserNotificationSettings:settings];
            [subscriber sendNext:@(YdkPermissionAuthorizationStatusAuthorized)];
            [subscriber sendCompleted];
            return nil;
        }];
    }
}

@end
