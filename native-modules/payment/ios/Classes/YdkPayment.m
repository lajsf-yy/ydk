//
//  YdkPayment.m
//  ydk-payment
//
//  Created by yryz on 2019/8/6.
//

#import "YdkPayment.h"
#import "YdkPaymentServiceProtocol.h"

#import "YdkApplePaySKService.h"
#import "YdkAlipayService.h"
#import "YdkWechatPayService.h"

#import <ydk-core/YdkCore.h>
#import <mob_sharesdk/WXApi.h>

NSErrorDomain const YdkPaymentErrorDomain = @"YdkPaymentErrorDomain";

@interface YdkPayment ()

@property (nonatomic, strong) id<YdkPaymentServiceProtocol> service;
@property (nonatomic, strong) id<YdkPaymentServiceProtocol> apService;

@end

@implementation YdkPayment
{
    NSString *_wechatAppKey;
    NSString *_universalLink;
}

+ (void)load {
    ydk_register_module(self);
}

- (instancetype)initWithConfig:(NSDictionary *)config {
    if (self = [super init]) {
        _wechatAppKey = [config valueForKeyPath:@"share.wechatAppId"];
        _universalLink = [config valueForKeyPath:@"universalLink"];
        _apService = [[YdkApplePaySKService alloc] init];
    }
    return self;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    [WXApi registerApp:_wechatAppKey universalLink:_universalLink];
    return YES;
}

// iOS 9.x 或更高版本
- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {
    if ([self.service respondsToSelector:@selector(handlerOpenURL:)]) {
        [self.service handlerOpenURL:url];
    }
    return YES;
}

- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void(^)(NSArray<id<UIUserActivityRestoring>> * __nullable restorableObjects))restorationHandler {
    if ([self.service respondsToSelector:@selector(handleOpenUniversalLink:)]) {
        [self.service handleOpenUniversalLink:userActivity];
    }
    return YES;
}

- (RACSignal<id> *)pay:(YdkPaymentType)type parameters:(NSDictionary *)parameters {
    id<YdkPaymentServiceProtocol> service = [self payService:type];
    if (service) {
        self.service = service;
        return [service payWithparameters:parameters];
    } else {
        self.service = nil;
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            [subscriber sendError:[NSError errorWithDomain:YdkPaymentErrorDomain code:YdkPaymentErrorNotSupport userInfo:@{NSLocalizedDescriptionKey : @"不支持的支付类型"}]];
            [subscriber sendCompleted];
            return nil;
        }];
    }
}

// MARK: -
- (id<YdkPaymentServiceProtocol>)payService:(YdkPaymentType)type {
    switch (type) {
        case YdkPaymentTypeApplePay_SK:
            return _apService;
            break;
        case YdkPaymentTypeAlipay:
            return [[YdkAlipayService alloc] init];
            break;
        case YdkPaymentTypeWechat:
            return [[YdkWechatPayService alloc] init];
            break;
        default:
            break;
    }
    return nil;
}

+ (RACSignal<id> *)pay:(YdkPaymentType)type parameters:(NSDictionary *)parameters {
    YdkPayment *payment = ydk_get_module_instance(self.class);
    return [payment pay:type parameters:parameters];
}

@end




