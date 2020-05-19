//
//  YdkPermissionServiceProtocol.h
//  ydk-permission
//
//  Created by yryz on 2019/7/11.
//

#import <Foundation/Foundation.h>
#import <ReactiveObjC/ReactiveObjC.h>

// 权限类型
typedef NS_ENUM(NSInteger, YdkPermissionAuthorizationType) {
    YdkPermissionAuthorizationTypeCamera = 0,
    YdkPermissionAuthorizationTypePhotoLibrary,
    YdkPermissionAuthorizationTypeMicrophone,
    YdkPermissionAuthorizationTypeLocationWhenInUse,
    YdkPermissionAuthorizationTypeContacts,
    YdkPermissionAuthorizationTypeNotification,
};

// 权限状态
typedef NS_ENUM(NSInteger, YdkPermissionAuthorizationStatus) {
    YdkPermissionAuthorizationStatusNotDetermined = 0,
    YdkPermissionAuthorizationStatusRestricted,
    YdkPermissionAuthorizationStatusDenied,
    YdkPermissionAuthorizationStatusAuthorized
};

FOUNDATION_EXPORT NSErrorDomain const YdkPermissionServiceErrorDomain;

NS_ERROR_ENUM(YdkPermissionServiceErrorDomain)
{
    YdkPermissionServiceErrorLocationServiceDisabled    = -1000, // 用户位置服务关闭
    YdkPermissionServiceErrorHealthKitNotSupportedOnDevice  = -1001,    // 此设备不支持HealthKit
};

@protocol YdkPermissionServiceProtocol <NSObject>

// 1. 获取权限状态
- (YdkPermissionAuthorizationStatus)permissionAuthorizationStatus;

// 2. 请求权限
- (RACSignal<NSNumber/*YdkPermissionAuthorizationStatus*/ *> *)requestAuthorization;

@end
