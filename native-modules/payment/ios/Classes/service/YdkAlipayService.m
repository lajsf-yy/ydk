//
//  YdkAlipayService.m
//  ydk-payment
//
//  Created by yryz on 2019/8/6.
//

#import "YdkAlipayService.h"
#import "YdkPaymentServiceProtocol.h"
#if !TARGET_OS_SIMULATOR
#import <AlipaySDK/AlipaySDK.h>
#endif
#import <ydk-core/YdkCore.h>

@interface YdkAlipayService ()

@property (nonatomic, strong) RACSubject *subject;

@end

@implementation YdkAlipayService

- (RACSignal<id> *)payWithparameters:(NSDictionary *)parameters {
    NSString *orderStr = [parameters objectForKey:@"orderStr"];
    NSString *scheme = [parameters objectForKey:@"URLScheme"];
    if (!scheme) {
        // 取出URLTypes第1个元素的URLScheme
        NSArray *URLTypes = [[NSBundle mainBundle].infoDictionary valueForKey:@"CFBundleURLTypes"];
        NSArray *URLSchemes = [URLTypes.firstObject objectForKey:@"CFBundleURLSchemes"];
        scheme = URLSchemes.firstObject;
    }
#if !TARGET_OS_SIMULATOR
    _subject = [RACSubject subject];
    @weakify(self);
    [[AlipaySDK defaultService] payOrder:orderStr fromScheme:scheme callback:^(NSDictionary *resultDic) {
        @strongify(self);
        [self handleCallbackResult:resultDic];
    }];
    return _subject;
#else
    return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
        NSError *error = [NSError errorWithDomain:YdkPaymentErrorDomain code:YdkPaymentErrorNotSupportSimulator userInfo:@{NSLocalizedDescriptionKey : @"不支持模拟器调试"}];
        [subscriber sendError:error];
        return nil;
    }];
#endif
}

- (void)handlerOpenURL:(NSURL *)url {
#if !TARGET_OS_SIMULATOR
    @weakify(self);
    [[AlipaySDK defaultService] processOrderWithPaymentResult:url standbyCallback:^(NSDictionary *resultDic) {
        @strongify(self);
        [self handleCallbackResult:resultDic];
    }];
#endif
}

// MARK: - 处理回调结果
- (void)handleCallbackResult:(NSDictionary *)resultDic {
    NSInteger resultStatus = [[resultDic objectForKey:@"resultStatus"] integerValue];
    NSString *memo = [resultDic objectForKey:@"memo"];
    if (resultStatus == 9000) {
        [self.subject sendNext:nil];
        [self.subject sendCompleted];
    } else if (resultStatus == 6001) {
        NSString *msg = (memo && memo.length > 0) ? memo : @"取消支付";
        NSError *error = [NSError errorWithDomain:YdkPaymentErrorDomain code:YdkCancel.integerValue userInfo:@{NSLocalizedDescriptionKey : msg}];
        [self.subject sendError:error];
    } else {
        NSString *msg = (memo && memo.length > 0) ? memo : @"支付失败";
        NSError *error = [NSError errorWithDomain:YdkPaymentErrorDomain code:resultStatus userInfo:@{NSLocalizedDescriptionKey : msg}];
        [self.subject sendError:error];
    }
}

@end
