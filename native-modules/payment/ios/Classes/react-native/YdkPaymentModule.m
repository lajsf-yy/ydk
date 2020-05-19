//
//  YdkPaymentModule.m
//  ydk-payment
//
//  Created by yryz on 2019/8/6.
//

#import "YdkPaymentModule.h"
#import "YdkPayment.h"
#import <React/RCTLog.h>

static NSString *kPaymentPurchased = @"paymentPurchased";

@implementation YdkPaymentModule
{
    BOOL _hasListeners;
}

- (void)startObserving {
    _hasListeners = YES;
}

- (void)stopObserving {
    _hasListeners = NO;
}

- (void)sendEventWithName:(NSString *)name body:(id)body {
    if (_hasListeners) {
        [super sendEventWithName:name body:body];
    }
}

- (instancetype)init {
    if (self = [super init]) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(handlePaymentTransactionPurchasedNotification:) name:YdkPaymentTransactionPurchasedNotification object:nil];
    }
    return self;
}

+ (BOOL)requiresMainQueueSetup {
    return true;
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

- (NSDictionary *)constantsToExport {
    return @{ kPaymentPurchased: kPaymentPurchased };
}

- (NSArray<NSString *> *)supportedEvents {
    return @[kPaymentPurchased];
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(pay:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    // payChannel ext
    YdkPaymentType type = [[data objectForKey:@"payChannel"] integerValue];
    NSDictionary *params = [data objectForKey:@"ext"];
    [[YdkPayment pay:type parameters:params] subscribeNext:^(NSString *receipt) {
        resolver(receipt);
    } error:^(NSError *error) {
        NSMutableDictionary *userInfo = [NSMutableDictionary dictionaryWithDictionary:error.userInfo];
        userInfo[@"msg"] = error.localizedDescription;
        NSError *e = [NSError errorWithDomain:error.domain code:error.code userInfo:userInfo];
        rejecter(@(e.code).stringValue, e.localizedDescription, e);
    }];
}

- (void)handlePaymentTransactionPurchasedNotification:(NSNotification *)noti {
    [self sendEventWithName:kPaymentPurchased body:noti.object];
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self name:YdkPaymentTransactionPurchasedNotification object:nil];
}

@end
