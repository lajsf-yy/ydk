//
//  YdkPermissionManager.m
//  ydk-permission
//
//  Created by yryz on 2019/7/11.
//

#import "YdkPermissionManager.h"

#import "YdkPermissionServiceCamera.h"
#import "YdkPermissionServicePhotoLibrary.h"
#import "YdkPermissionServiceMicrophone.h"
#import "YdkPermissionServiceLocationWhenInUse.h"
#import "YdkPermissionServiceContacts.h"
#import "YdkPermissionServiceNotification.h"

NSErrorDomain const YdkPermissionErrorDomain = @"YdkPermissionErrorDomain";
NSErrorDomain const YdkPermissionServiceErrorDomain = @"YdkPermissionServiceErrorDomain";

@implementation YdkPermissionManager

static id<YdkPermissionServiceProtocol> staticService;

// 1. 获取权限状态
+ (RACSignal<NSNumber/*PermissionAuthorizationStatus*/ *> *)permissionAuthorizationStatus:(YdkPermissionAuthorizationType)type {
    return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
        id<YdkPermissionServiceProtocol> service = [self permissionService:type];
        if (service) {
            [subscriber sendNext:@([service permissionAuthorizationStatus])];
            [subscriber sendCompleted];
        } else {
            [subscriber sendError:[NSError errorWithDomain:YdkPermissionErrorDomain code:YdkPermissionErrorNotSupportType userInfo:@{NSLocalizedDescriptionKey : @"不支持的请求权限类型"}]];
        }
        return nil;
    }];
}

// 2. 请求权限并返回权限状态
+ (RACSignal *)requestAuthorization:(YdkPermissionAuthorizationType)type {
    id<YdkPermissionServiceProtocol> service = [self permissionService:type];
    if (service) {
        // 定位的特殊性:delegate
        if (type == YdkPermissionAuthorizationTypeLocationWhenInUse) {
            staticService = service;
        } else {
            staticService = nil;
        }
        return [service requestAuthorization];
    } else {
        staticService = nil;
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            [subscriber sendError:[NSError errorWithDomain:YdkPermissionErrorDomain code:YdkPermissionErrorNotSupportType userInfo:@{NSLocalizedDescriptionKey : @"不支持的请求权限类型"}]];
            return nil;
        }];
    }
}

// MARK: -
+ (id<YdkPermissionServiceProtocol>)permissionService:(YdkPermissionAuthorizationType)type {
    switch (type) {
        case YdkPermissionAuthorizationTypeCamera:
            return [YdkPermissionServiceCamera new];
            break;
        case YdkPermissionAuthorizationTypePhotoLibrary:
            return [YdkPermissionServicePhotoLibrary new];
            break;
        case YdkPermissionAuthorizationTypeMicrophone:
            return [YdkPermissionServiceMicrophone new];
            break;
        case YdkPermissionAuthorizationTypeLocationWhenInUse:
            return [YdkPermissionServiceLocationWhenInUse new];
            break;
        case YdkPermissionAuthorizationTypeContacts:
            return [YdkPermissionServiceContacts new];
            break;
        case YdkPermissionAuthorizationTypeNotification:
            return [YdkPermissionServiceNotification new];
            break;
        default:
            break;
    }
    return nil;
}

@end
