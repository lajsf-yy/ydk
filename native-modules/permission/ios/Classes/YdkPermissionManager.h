//
//  YdkPermissionManager.h
//  ydk-permission
//
//  Created by yryz on 2019/7/11.
//

#import <Foundation/Foundation.h>
#import "YdkPermissionServiceProtocol.h"

FOUNDATION_EXPORT NSErrorDomain const YdkPermissionErrorDomain;

NS_ERROR_ENUM(YdkPermissionErrorDomain)
{
    YdkPermissionErrorNotSupportType                   = -1000,     // 不支持的请求权限类型
};

@interface YdkPermissionManager : NSObject

// 1. 获取权限状态
+ (RACSignal<NSNumber/*YdkPermissionAuthorizationStatus*/ *> *)permissionAuthorizationStatus:(YdkPermissionAuthorizationType)type;

// 2. 请求权限
+ (RACSignal<NSNumber/*YdkPermissionAuthorizationStatus*/ *> *)requestAuthorization:(YdkPermissionAuthorizationType)type;

@end
