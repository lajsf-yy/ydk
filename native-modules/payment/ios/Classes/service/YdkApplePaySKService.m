//
//  YdkApplePaySKService.m
//  ydk-payment
//
//  Created by yryz on 2019/8/6.
//

#import "YdkApplePaySKService.h"

#import <StoreKit/StoreKit.h>
#import <SVProgressHUD/SVProgressHUD.h>
#import <ydk-core/YdkCore.h>

NSString const* YdkApplePayProductIdentifier = @"productIdentifier";
NSString *const YdkPaymentTransactionPurchasedNotification = @"YdkPaymentTransactionPurchasedNotification";

@interface YdkApplePaySKService () <SKPaymentTransactionObserver, SKProductsRequestDelegate>

@end

@implementation YdkApplePaySKService {
    RACSubject *_subject;
    NSString *_userName;
}

- (instancetype)init {
    if (self =[super init]) {
        [[SKPaymentQueue defaultQueue] addTransactionObserver:self];
    }
    return self;
}

- (RACSignal<id> *)payWithparameters:(NSDictionary *)parameters {
    NSArray *productIdentifiers = @[parameters[YdkApplePayProductIdentifier]];
    _userName = [parameters objectForKey:@"id"];
    if ([SKPaymentQueue canMakePayments] && productIdentifiers.count) {
        SKProductsRequest *request = [[SKProductsRequest alloc] initWithProductIdentifiers:[NSSet setWithArray:productIdentifiers]];
        request.delegate = self;
        // step1 创建一个商品查询的请求
        [request start];
        _subject = [RACSubject subject];
        
        [SVProgressHUD setDefaultMaskType:SVProgressHUDMaskTypeClear];
        [SVProgressHUD showWithStatus:@"正在连接Apple支付"];
        return _subject;
    } else {
        return [RACSignal createSignal:^RACDisposable * (id<RACSubscriber> subscriber) {
            [subscriber sendError:[NSError errorWithDomain:YdkPaymentErrorDomain code:YdkPaymentErrorMakePaymentsFailed userInfo:@{NSLocalizedDescriptionKey : @"NO if this device is not able or allowed to make payments"}]];
            return nil;
        }];
    }
}

// MARK: - SKProductsRequestDelegate

// step1 收到产品返回信息
- (void)productsRequest:(SKProductsRequest *)request didReceiveResponse:(SKProductsResponse *)response {
    if (!response || !response.products || ![response.products count]) {
        return;
    }
    // stpe2 创建内购
    SKMutablePayment *payment = [SKMutablePayment paymentWithProduct:response.products.firstObject];
    payment.applicationUsername = _userName;
    [[SKPaymentQueue defaultQueue] addPayment:payment];
    [SVProgressHUD showWithStatus:@"正在支付"];
}

// step1 获取请求完成
- (void)requestDidFinish:(SKRequest *)request {
    // [SVProgressHUD dismiss];
}

// step1 获取请求失败
- (void)request:(SKRequest *)request didFailWithError:(NSError *)error {
    [SVProgressHUD dismiss];
    if (_subject) {
        [_subject sendError:error];
        _subject = nil;
    }
}

// MARK: - SKPaymentTransactionObserver
- (void)paymentQueue:(SKPaymentQueue *)queue updatedTransactions:(NSArray<SKPaymentTransaction *> *)transactions {
    for (SKPaymentTransaction *tran in transactions) {
        switch (tran.transactionState) {
            case SKPaymentTransactionStatePurchased: {
                [SVProgressHUD dismiss];
                NSDictionary *body = @{ @"receipt" : [self receiptString] ? : @"",
                                        @"productIdentifier" : tran.payment.productIdentifier ? : @"",
                                        @"transactionIdentifier" : tran.transactionIdentifier ? : @"",
                                        @"id" : tran.payment.applicationUsername ? : @""
                };
                if (_subject) {
                    [_subject sendNext:body];
                    [_subject sendCompleted];
                    _subject = nil;
                    [[SKPaymentQueue defaultQueue] finishTransaction:tran];
                } else {
                    // 防止rn未初始化完成
                    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(3 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                        [[NSNotificationCenter defaultCenter] postNotificationName:YdkPaymentTransactionPurchasedNotification object:body];
                        [[SKPaymentQueue defaultQueue] finishTransaction:tran];
                    });
                }
            }
                break;
            case SKPaymentTransactionStatePurchasing:
                // 商品添加进列表
                [SVProgressHUD showWithStatus:@"正在支付"];
                break;
            case SKPaymentTransactionStateRestored: {
                // 已经购买过商品
                [SVProgressHUD dismiss];
                if (_subject) {
                    [_subject sendError:tran.error];
                    _subject = nil;
                }
                [[SKPaymentQueue defaultQueue] finishTransaction:tran];
            }
                break;
            case SKPaymentTransactionStateFailed: {
                [SVProgressHUD dismiss];
                if (_subject) {
                    if ([tran.error.domain isEqualToString:SKErrorDomain] && tran.error.code == SKErrorPaymentCancelled) {
                        // 取消
                        [_subject sendError:[NSError errorWithDomain:YdkPaymentErrorDomain code:YdkCancel.integerValue userInfo:@{ NSLocalizedDescriptionKey : @"取消支付" }]];
                    } else {
                        [_subject sendError:tran.error];
                    }
                    _subject = nil;
                }
                [[SKPaymentQueue defaultQueue] finishTransaction:tran];
            }
                break;
            default:
                break;
        }
    }
}

- (NSString *)receiptString {
    NSURL *receiptURL = [[NSBundle mainBundle] appStoreReceiptURL];
    NSData *receipt = [NSData dataWithContentsOfURL:receiptURL];
    if (!receipt) {
        return nil;
    }
    return [receipt base64EncodedStringWithOptions:0];
}

- (void)dealloc {
    [[SKPaymentQueue defaultQueue] removeTransactionObserver:self];
}

@end
