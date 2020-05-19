//
//  YdkPermissionModule.m
//  ydk-permission
//
//  Created by yryz on 2019/7/11.
//

#import "YdkPermissionModule.h"
#import "YdkPermissionManager.h"

@implementation YdkPermissionModule
{
    NSDictionary *_typeMapping;
    NSDictionary *_statusMapping;
}

- (instancetype)init {
    if (self = [super init]) {
        _typeMapping = @{@"camera" : @(YdkPermissionAuthorizationTypeCamera),
                         @"photoLibrary" : @(YdkPermissionAuthorizationTypePhotoLibrary),
                         @"microphone" : @(YdkPermissionAuthorizationTypeMicrophone),
                         @"locationWhenInUse" : @(YdkPermissionAuthorizationTypeLocationWhenInUse),
                         @"contacts" : @(YdkPermissionAuthorizationTypeContacts),
                         @"notification" : @(YdkPermissionAuthorizationTypeNotification)};
        _statusMapping = @{@(YdkPermissionAuthorizationStatusNotDetermined) : @"notDetermined",
                           @(YdkPermissionAuthorizationStatusRestricted) : @"restricted",
                           @(YdkPermissionAuthorizationStatusDenied) : @"denied",
                           @(YdkPermissionAuthorizationStatusAuthorized) : @"authorized"};
    }
    return self;
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(getPermission:(NSString *)t resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    YdkPermissionAuthorizationType type = [[_typeMapping objectForKey:t] integerValue];
    [[YdkPermissionManager permissionAuthorizationStatus:type] subscribeNext:^(NSNumber *x) {
        NSString *status = [self->_statusMapping objectForKey:x];
        resolver(status);
    } error:^(NSError * error) {
        rejecter(@(error.code).stringValue, [error.userInfo objectForKey:NSLocalizedDescriptionKey], error);
    }];
}

RCT_EXPORT_METHOD(requestPermission:(NSString *)t resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    YdkPermissionAuthorizationType type = [[_typeMapping objectForKey:t] integerValue];
    [[YdkPermissionManager requestAuthorization:type] subscribeNext:^(id  _Nullable x) {
        NSString *status = [self->_statusMapping objectForKey:x];
        resolver(status);
    } error:^(NSError * _Nullable error) {
        rejecter(@(error.code).stringValue, [error.userInfo objectForKey:NSLocalizedDescriptionKey], error);
    }];
}

@end
