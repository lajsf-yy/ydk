//
//  YdkQRCodeReactView.m
//  ydk-qrcode
//
//  Created by yryz on 2019/8/19.
//

#import "YdkQRCodeReactView.h"

#import <React/UIView+React.h>
#import <React/RCTLog.h>
#import <ydk-toolkit/YdkToolkit.h>

@implementation YdkQRCodeReactView

RCT_NOT_IMPLEMENTED(- (instancetype)initWithFrame:(CGRect)frame)
RCT_NOT_IMPLEMENTED(- (instancetype)initWithCoder:(NSCoder *)aDecoder)

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher {
    RCTAssertParam(eventDispatcher);
    if ((self = [super initWithFrame:CGRectZero])) {
        _qrCodeView = [[YdkQRCodeView alloc] initWithFrame:CGRectMake(0, 0, [UIScreen mainScreen].bounds.size.width, 0)];
        __weak typeof(self) weakSelf = self;
        _qrCodeView.scanResult = ^(NSString *result) {
            if (weakSelf.onResult) {
                NSDictionary *body = @{@"codeInfo" : result ? : @""};
                RCTLog(@"result: %@", body);
                weakSelf.onResult(body);
            }
        };
        _qrCodeView.errorBlock = ^(NSError *error) {
            NSDictionary *body = @{@"domain": error.domain,
                                   @"code": @(error.code),
                                   @"description": error.localizedDescription ? : error.localizedDescription};
            RCTLog(@"error: %@", body);
            if (weakSelf.onError) {
                weakSelf.onError(body);
            } else {
                dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(1 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
                    if (weakSelf.onError) {
                        weakSelf.onError(body);
                    }
                });
            }
        };
        [self addSubview:_qrCodeView];
    }
    return self;
}

static const NSString *kBoxColor = @"boxColor";
static const NSString *kAngleColor = @"angleColor";
static const NSString *kLineColor = @"lineColor";
static const NSString *kWidth= @"width";
static const NSString *kHeight = @"height";
static const NSString *kTopOffset= @"topOffset";

- (void)setConfig:(NSDictionary *)config {
    _config = [config copy];
    // 外框
    NSString *borderS = [config objectForKey:kBoxColor];
    if (borderS && [borderS isKindOfClass:[NSString class]]) {
        _qrCodeView.scanBorderColor = [UIColor tk_colorWithHexString:borderS];
    }
    // 四个角
    NSString *cornerS= [config objectForKey:kAngleColor];
    if (cornerS && [cornerS isKindOfClass:[NSString class]]) {
        _qrCodeView.scanCornerColor = [UIColor tk_colorWithHexString:cornerS];
    }
    // 扫描线
    NSString *lineS = [config objectForKey:kLineColor];
    if (lineS && [lineS isKindOfClass:[NSString class]]) {
        _qrCodeView.scanLineColor = [UIColor tk_colorWithHexString:lineS];
    }
    // 宽高
    CGFloat width = [[config objectForKey:kWidth] doubleValue];
    CGFloat height = [[config objectForKey:kHeight] doubleValue];
    if (width > 0 && height > 0) {
        _qrCodeView.scanSize = CGSizeMake(width, height);
    }
    // 顶部
    CGFloat topMargin = [[config objectForKey:kTopOffset] doubleValue];
    _qrCodeView.scanTopMargin = topMargin;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    _qrCodeView.frame = self.bounds;
}

@end
