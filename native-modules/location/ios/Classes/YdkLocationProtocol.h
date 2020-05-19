//
//  YdkLocationProtocol.h
//  ydk-location
//
//  Created by yryz on 2019/7/25.
//

#import <Foundation/Foundation.h>
#import <ReactiveObjC/ReactiveObjC.h>

#import "YdkLocationInfo.h"

FOUNDATION_EXPORT NSErrorDomain const YdkLocationErrorDomain;

NS_ERROR_ENUM(YdkLocationErrorDomain)
{
    YdkLocationErrorAuthorizationDenied            = -1001,     // 未授权
    YdkLocationErrorReverseGeocodeFail             = -1002,     // 反地理编码失败
    YdkLocationErrorReverseGeocodeNotPlacemark     = -1003,     // 未获取到反地理编码地区信息
    YdkLocationErrorLocationRequestFailed          = -1004,     // 添加单次定位Request失败
};

@protocol YdkLocationProtocol <NSObject>

/**
 获取定位地理信息
 
 @return 地理信息
 */
- (RACSignal<YdkLocationInfo *> *)requestLocation;

@end
