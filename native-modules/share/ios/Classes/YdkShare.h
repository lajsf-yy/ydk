#import <Foundation/Foundation.h>
#import <ydk-core/YdkCore.h>

typedef enum : NSUInteger {
    YdkShareContentTypeAuto         = 0,    // 自动适配类型，视传入的参数来决定
    YdkShareContentTypeText         = 1,    // 文本
    YdkShareContentTypeImage        = 2,    // 图片
    YdkShareContentTypeAudio        = 5,    // 音频
    YdkShareContentTypeVideo        = 6,    // 视频
} YdkShareContentType;

@interface YdkShareData:NSObject

@property (nonatomic, retain) NSString *title;
@property (nonatomic, retain)  NSString *content;
@property (nonatomic, retain)  NSString *url;
@property (nonatomic, retain)  NSString *imgUrl;
@property (nonatomic, retain)  NSString* contentType;
@property (nonatomic, retain)  NSString *path;
@property (nonatomic, retain)  NSString *thumbImage;
@property (nonatomic, retain)  NSString *hdThumbImage;
@property (nonatomic, retain) NSNumber* miniProgramType;

@end

FOUNDATION_EXPORT NSErrorDomain const YdkShareErrorDomain;
NS_ERROR_ENUM(YdkShareErrorDomain)
{
    YdkShareErrorPlatformUnknown = -1000,   // 未知分享平台
    YdkShareErrorShareFailure = -1001,      // 第三方分享失败
    YdkShareErrorLoginFailure = -1002,      // 第三方登录失败
    YdkShareErrorNotSupportAuthorizePlatform = -1003,      // 不支持三方授权平台
    YdkShareErrorAuthorizeFailed = -1004,   // 授权失败
};
@interface YdkShare : NSObject<YdkModule>

/**
 第三方分享
 
 @param platform   分享第三方平台 qq|qZone|weChat|weChatMoment|sinaWeibo
 @param shareData 分享数据
 @param resolve    处理完成后的成功回调
 @param reject     处理完成后的失败回调
 */
+ (void)share:(NSString *)platform shareData:(YdkShareData *)shareData
resolve:(YdkResolveBlock)resolve reject:(YdkRejectBlock)reject;

/**
 第三方登录
 
 @param platform   分享第三方平台 qq|qZone|weChat|weChatMoment|sinaWeibo
 @param resolve    处理完成后的成功回调
 @param reject     处理完成后的失败回调
 */
+ (void)authorizeLogin:(NSString*)platform resolve:(YdkResolveBlock)resolve reject:(YdkRejectBlock)reject;

/**
 第三方授权
 
 @param platform   分享第三方平台 qq|qZone|weChat|weChatMoment|sinaWeibo
 @param resolve    处理完成后的成功回调
 @param reject     处理完成后的失败回调
 */
+ (void)authorize:(NSString*)platform resolve:(YdkResolveBlock)resolve reject:(YdkRejectBlock)reject;

/**
 所有安装的分享平台[此方案仅在info.plist文件中注册过白名单有效]
 */
+ (void)getInstallPlatforms:(YdkResolveBlock)resolve reject:(YdkRejectBlock)reject;

@end

