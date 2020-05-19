//
//  YdkWechatPayService.m
//  ydk-payment
//
//  Created by yryz on 2019/8/6.
//

#import "YdkWechatPayService.h"

#import <mob_sharesdk/WXApi.h>
#import <mob_sharesdk/WXApiObject.h>
#import <ydk-core/YdkCore.h>

@interface YdkWechatPayService() <WXApiDelegate>

@property (nonatomic, strong) RACSubject *subject;

@end

@implementation YdkWechatPayService

- (RACSignal<id> *)payWithparameters:(NSDictionary *)parameters {
    // 获取微信支付参数
    NSString *stamp  = [NSString stringWithFormat:@"%@", [parameters objectForKey:@"timestamp"]];
    NSString *partnerid = [NSString stringWithFormat:@"%@", [parameters objectForKey:@"partnerid"]];
    NSString *appid = [NSString stringWithFormat:@"%@", [parameters objectForKey:@"appid"]];
    NSString *prepayid = [NSString stringWithFormat:@"%@", [parameters objectForKey:@"prepayid"]];
    NSString *noncestr = [NSString stringWithFormat:@"%@", [parameters objectForKey:@"noncestr"]];
    NSString *package = [NSString stringWithFormat:@"%@", [parameters objectForKey:@"package"]];
    NSString *sign = [NSString stringWithFormat:@"%@", [parameters objectForKey:@"sign"]];
    
    PayReq *req   = [[PayReq alloc] init];
    req.openID    = appid;
    req.partnerId = partnerid;
    req.prepayId  = prepayid;
    req.nonceStr  = noncestr;
    req.timeStamp = stamp.intValue;
    req.package   = package;
    req.sign      = sign;
    
    @weakify(self);
    _subject = [RACSubject subject];
    dispatch_async(dispatch_get_global_queue(DISPATCH_TARGET_QUEUE_DEFAULT, 0), ^{
        @strongify(self);
        
        @weakify(self);
        [WXApi sendReq:req completion:^(BOOL success) {
            if (!success) {
                @strongify(self);
                NSError *error = [NSError errorWithDomain:YdkPaymentErrorDomain code:YdkPaymentErrorOpenFailed userInfo:@{NSLocalizedDescriptionKey : @"调起支付失败"}];
                [self.subject sendError:error];
            }
        }];
    });
    return _subject;
}

/// 微信支付后 回调
- (void)onResp:(BaseResp *)resp {
    if (resp.errCode == WXSuccess) {
        [self.subject sendNext:nil];
        [self.subject sendCompleted];
    } else if (resp.errCode == WXErrCodeUserCancel) {
        NSError *error = [NSError errorWithDomain:YdkPaymentErrorDomain code:YdkCancel.integerValue userInfo:@{NSLocalizedDescriptionKey : resp.errStr ? : @"取消支付"}];
        [self.subject sendError:error];
    } else {
        NSError *error = [NSError errorWithDomain:YdkPaymentErrorDomain code:resp.errCode userInfo:@{NSLocalizedDescriptionKey : resp.errStr ? : @"支付失败"}];
        [self.subject sendError:error];
    }
}

- (void)handlerOpenURL:(NSURL *)url {
    [WXApi handleOpenURL:url delegate:self];
}

- (void)handleOpenUniversalLink:(NSUserActivity *)userActivity {
    [WXApi handleOpenUniversalLink:userActivity delegate:self];
}

@end
