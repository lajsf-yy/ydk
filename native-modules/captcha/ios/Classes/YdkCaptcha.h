//
//  YdkCaptcha.h
//  ydk-captcha
//
//  Created by yryz on 2019/7/12.
//

#import <Foundation/Foundation.h>
#import <ydk-core/YdkCore.h>
#import <ReactiveObjC/ReactiveObjC.h>

FOUNDATION_EXPORT NSErrorDomain const YdkCaptchaErrorDomain;
NS_ERROR_ENUM(YdkCaptchaErrorDomain)
{
    YdkCaptchaErrorInvalidParams        = -1000,     // 无效的参数
    YdkCaptchaErrorLackParams           = -1001,     // 缺少参数
};

@interface YdkCaptcha : NSObject <YdkModule>

- (RACSignal *)start:(NSString *)phone;

- (void)stop;

@end
