//
//  YdkPermissionServiceLocationWhenInUse.m
//  ydk-permission
//
//  Created by yryz on 2019/7/11.
//

#import "YdkPermissionServiceLocationWhenInUse.h"

@interface YdkPermissionServiceLocationWhenInUse() <CLLocationManagerDelegate>

@end

@implementation YdkPermissionServiceLocationWhenInUse
{
    CLLocationManager *_locationManager;
    RACSubject *_subject;
}

- (instancetype)init {
    if (self = [super init]) {
        _locationManager = [[CLLocationManager alloc] init];
    }
    return self;
}

// 1. 获取权限状态
- (YdkPermissionAuthorizationStatus)permissionAuthorizationStatus {
    if ([CLLocationManager locationServicesEnabled]) {
        CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
        return [self permissionStatus:status];
    } else {
        // 用户关闭位置服务
        return YdkPermissionAuthorizationStatusDenied;
    }
}

- (YdkPermissionAuthorizationStatus)permissionStatus:(CLAuthorizationStatus)status {
    switch (status) {
        case kCLAuthorizationStatusNotDetermined:
            return YdkPermissionAuthorizationStatusNotDetermined;
            break;
        case kCLAuthorizationStatusRestricted:
            return YdkPermissionAuthorizationStatusRestricted;
            break;
        case kCLAuthorizationStatusDenied:
            return YdkPermissionAuthorizationStatusDenied;
            break;
        case kCLAuthorizationStatusAuthorizedAlways:
        case kCLAuthorizationStatusAuthorizedWhenInUse:
            return YdkPermissionAuthorizationStatusAuthorized;
            break;
    }
}

// 2. 请求权限并返回权限状态
- (RACSignal<NSNumber/*YdkPermissionAuthorizationStatus*/ *> *)requestAuthorization {
    if ([CLLocationManager locationServicesEnabled]) {
        CLAuthorizationStatus status = [CLLocationManager authorizationStatus];
        if (status == kCLAuthorizationStatusNotDetermined) {
            _locationManager.delegate = self;
            [_locationManager requestWhenInUseAuthorization];
            _subject = [RACSubject subject];
            return _subject;
        } else {
            return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
                [subscriber sendNext:@([self permissionStatus:status])];
                [subscriber sendCompleted];
                return nil;
            }];
        }
    } else {
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            [subscriber sendError:[NSError errorWithDomain:YdkPermissionServiceErrorDomain code:YdkPermissionServiceErrorLocationServiceDisabled userInfo:@{NSLocalizedDescriptionKey : @"用户位置服务关闭"}]];
            return nil;
        }];
    }
}

- (void)locationManager:(CLLocationManager *)manager didChangeAuthorizationStatus:(CLAuthorizationStatus)status {
    if (status == kCLAuthorizationStatusNotDetermined) {
        return;
    }
    [_subject sendNext:@([self permissionStatus:status])];
    [_subject sendCompleted];
}

- (void)dealloc {
    _locationManager = nil;
}

@end

