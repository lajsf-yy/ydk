//
//  YdkPayment.h
//  ydk-payment
//
//  Created by yryz on 2019/8/6.
//

#import <Foundation/Foundation.h>
#import <ReactiveObjC/ReactiveObjC.h>

// 支付类型
typedef NS_ENUM(NSInteger, YdkPaymentType) {
    YdkPaymentTypeAlipay = 1,
    YdkPaymentTypeWechat,
    YdkPaymentTypeApplePay_SK,  // 内购
    YdkPaymentTypeApplePay_PK,  // apple pay
};

FOUNDATION_EXTERN NSString *const YdkPaymentTransactionPurchasedNotification;

@interface YdkPayment : NSObject

+ (RACSignal<id> *)pay:(YdkPaymentType)type parameters:(NSDictionary *)parameters;

@end
