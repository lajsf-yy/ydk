//
//  YdkPaymentServiceProtocol.h
//  ydk-payment
//
//  Created by yryz on 2019/8/6.
//

#import <Foundation/Foundation.h>
#import <ReactiveObjC/ReactiveObjC.h>

FOUNDATION_EXPORT NSErrorDomain const YdkPaymentErrorDomain;

NS_ERROR_ENUM(YdkPaymentErrorDomain)
{
    YdkPaymentErrorNotSupport               = -1000, // 不支持的支付类型
    YdkPaymentErrorMakePaymentsFailed       = -1001, // NO if this device is not able or allowed to make payments
    YdkPaymentErrorResubmit                 = -1002, // 重复提交购买
    YdkPaymentErrorOpenFailed               = -1003, // 调起支付页面失败
    YdkPaymentErrorNotSupportSimulator      = -1004, // 不支持模拟器
};

@protocol YdkPaymentServiceProtocol <NSObject>

- (RACSignal<id> *)payWithparameters:(NSDictionary *)parameters;

@optional
- (void)handlerOpenURL:(NSURL *)url;
- (void)handleOpenUniversalLink:(NSUserActivity *)userActivity;

@end
