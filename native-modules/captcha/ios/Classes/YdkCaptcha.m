//
//  YdkCaptcha.m
//  ydk-captcha
//
//  Created by yryz on 2019/7/12.
//

#import "YdkCaptcha.h"

#import <ydk-network/YdkNetwork.h>
#import <GT3Captcha/GT3Captcha.h>

NSErrorDomain const YdkCaptchaErrorDomain = @"YdkCaptchaErrorDomain";

@interface YdkCaptcha () <GT3CaptchaManagerDelegate, GT3CaptchaManagerViewDelegate>

@property (nonatomic, copy) NSString *api1;
@property (nonatomic, copy) NSString *api2;

@property (nonatomic, strong) GT3CaptchaManager *manager;
@property (nonatomic, assign) BOOL start;
@property (nonatomic, copy) NSString *phone;

@property (nonatomic, strong) RACSubject *subject;

@end

@implementation YdkCaptcha

+ (void)load {
    ydk_register_module(self);
}

- (instancetype)initWithConfig:(NSDictionary *)config {
    self = [super init];
    if (self) {
        _api1 = @"/pb/geetest/action/pre-process";
        _api2 = @"/pb/geetest/action/check";
    }
    return self;
}

- (GT3CaptchaManager *)manager {
    if (!_manager) {
        _manager = [[GT3CaptchaManager alloc] initWithAPI1:_api1 API2:_api2 timeout:15.0];
        _manager.delegate = self;
        _manager.viewDelegate = self;
        // [_manager enableDebugMode:YES];
        [_manager useVisualViewWithEffect:[UIBlurEffect effectWithStyle:UIBlurEffectStyleDark]];
    }
    return _manager;
}

// MARK: - Public Method
- (RACSignal *)start:(NSString *)phone {
    if (!phone || ![phone isKindOfClass:[NSString class]]) {
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            NSError *error = [NSError errorWithDomain:YdkCaptchaErrorDomain code:YdkCaptchaErrorInvalidParams userInfo:@{NSLocalizedDescriptionKey : @"phone参数有误"}];
            [subscriber sendError:error];
            return nil;
        }];
    }
    _phone = [phone copy];
    _start = YES;
    return [self startGTCaptcha];
}

- (void)stop {
    self.start = NO;
    [self.manager stopGTCaptcha];
    [self.manager closeGTViewIfIsOpen];
}

// !!!: 不要重复调用
- (RACSignal *)startGTCaptcha {
    _subject = [RACSubject subject];
    YdkNetwork *network= ydk_get_module_instance(YdkNetwork.class);
    
    @weakify(self);
    [[network request:GET service:@"platform-support" URLString:_api1 parameters:@{@"verifyKey" : _phone}] subscribeNext:^(id x) {
        @strongify(self);
        NSString *geetest_id = [x objectForKey:@"gt"];
        NSString *geetest_challenge = [x objectForKey:@"challenge"];
        NSNumber *geetest_success = [x objectForKey:@"success"];
        if (geetest_id && geetest_challenge && geetest_success) {
            [self.manager configureGTest:geetest_id challenge:geetest_challenge success:geetest_success withAPI2:self.api2];
            [self.manager startGTCaptchaWithAnimated:YES];
        } else {
            NSError *error = [NSError errorWithDomain:YdkCaptchaErrorDomain code:YdkCaptchaErrorLackParams userInfo:@{NSLocalizedDescriptionKey : @"api1接口缺少参数"}];
            [self.subject sendError:error];
        }
    } error:^(NSError *error) {
        @strongify(self);
        [self.subject sendError:error];
    }];
    return _subject;
}

- (void)gtCaptcha:(GT3CaptchaManager *)manager errorHandler:(GT3Error *)error {
    if (!_start) return;
    
    _start = NO;
    NSError *e;
    if (error.code == -999) {
        // 请求被意外中断, 一般由用户进行取消操作导致, 可忽略错误
        e = [NSError errorWithDomain:YdkCaptchaErrorDomain code:YdkCancel.integerValue userInfo:@{NSLocalizedDescriptionKey : @"验证取消"}];
    } else if (error.code == -10) {
        // 预判断时被封禁, 不会再进行图形验证
        
    } else if (error.code == -20) {
        // 尝试过多
        
    } else {
        // 网络问题或解析失败, 更多错误码参考开发文档
        
    }
    if (!e)
        e = [NSError errorWithDomain:YdkCaptchaErrorDomain code:error.code userInfo:@{NSLocalizedDescriptionKey : @"验证失败"}];
    [self.subject sendError:e];
}

- (void)gtCaptchaUserDidCloseGTView:(GT3CaptchaManager *)manager {
    _start = NO;
    NSError *error = [NSError errorWithDomain:YdkCaptchaErrorDomain code:YdkCancel.integerValue userInfo:@{NSLocalizedDescriptionKey : @"验证取消"}];
    [self.subject sendError:error];
}

- (void)gtCaptcha:(GT3CaptchaManager *)manager didReceiveSecondaryCaptchaData:(NSData *)data response:(NSURLResponse *)response error:(GT3Error *)error decisionHandler:(void (^)(GT3SecondaryCaptchaPolicy))decisionHandler {
    if (!error) {
        // NSLog(@"\ndata: %@", [[NSString alloc] initWithData:data encoding:NSUTF8StringEncoding]);
        decisionHandler(GT3SecondaryCaptchaPolicyAllow);
        _start = NO;
        [self.subject sendNext:nil];
        [self.subject sendCompleted];
    } else {
        // 二次验证发生错误
        decisionHandler(GT3SecondaryCaptchaPolicyForbidden);
        NSError *e = [NSError errorWithDomain:YdkCaptchaErrorDomain code:error.code userInfo:@{NSLocalizedDescriptionKey : @"验证取消"}];
        [self.subject sendError:e];
    }
}

//// 修改API1的请求
//- (void)gtCaptcha:(GT3CaptchaManager *)manager willSendRequestAPI1:(NSURLRequest *)originalRequest withReplacedHandler:(void (^)(NSURLRequest *))replacedHandler {
//    NSMutableURLRequest *mRequest = [originalRequest mutableCopy];
//    NSMutableDictionary *header = [originalRequest.allHTTPHeaderFields mutableCopy];
//    if (_phone) {
//        NSString *newURL = [NSString stringWithFormat:@"%@?verifyKey=%@", originalRequest.URL.absoluteString, _phone];
//        mRequest.URL = [NSURL URLWithString:newURL];
//        [header setObject:@"tenantId" forKey:@"lovelorn"];
//    }
//    replacedHandler(mRequest);
//}
//
//// 修改API2的请求
//- (void)gtCaptcha:(GT3CaptchaManager *)manager willSendSecondaryCaptchaRequest:(NSURLRequest *)originalRequest withReplacedRequest:(void (^)(NSMutableURLRequest *))replacedRequest {
//    NSMutableURLRequest *mRequest = [originalRequest mutableCopy];
//    NSMutableDictionary *header = [originalRequest.allHTTPHeaderFields mutableCopy];
//    if (_phone) {
//        NSString *newURL = [NSString stringWithFormat:@"%@?verifyKey=%@", originalRequest.URL.absoluteString, _phone];
//        mRequest.URL = [NSURL URLWithString:newURL];
//        [header setObject:@"tenantId" forKey:@"lovelorn"];
//    }
//    replacedRequest(mRequest);
//}

// 不使用默认的二次验证接口
- (void)gtCaptcha:(GT3CaptchaManager *)manager didReceiveCaptchaCode:(NSString *)code result:(NSDictionary *)result message:(NSString *)message {
    YdkNetwork *network= ydk_get_module_instance(YdkNetwork.class);
    @weakify(self);
    NSMutableDictionary *params = [NSMutableDictionary dictionaryWithDictionary:result];
    [params setObject:_phone forKey:@"verifyKey"];
    [[network request:POST service:@"platform-support" URLString:_api2 parameters:params] subscribeNext:^(id x) {
        @strongify(self);
        [self.subject sendNext:nil];
        [self.subject sendCompleted];
    } error:^(NSError *error) {
        @strongify(self);
        [self.subject sendError:error];
    }];
}

- (BOOL)shouldUseDefaultSecondaryValidate:(GT3CaptchaManager *)manager {
    return NO;
}

- (void)gtCaptcha:(GT3CaptchaManager *)manager updateCaptchaStatus:(GT3CaptchaState)state error:(GT3Error *)error {
    switch (state) {
        case GT3CaptchaStateInactive:
        case GT3CaptchaStateActive:
        case GT3CaptchaStateComputing: {
            // [self showIndicator];
            break;
        }
        case GT3CaptchaStateInitial:
        case GT3CaptchaStateFail:
        case GT3CaptchaStateError:
        case GT3CaptchaStateSuccess:
        case GT3CaptchaStateCancel: {
            // [self removeIndicator];
            break;
        }
        case GT3CaptchaStateWaiting:
        case GT3CaptchaStateCollecting:
        default: {
            break;
        }
    }
}

@end
