//
//  YdkQRCodeReactView.h
//  ydk-qrcode
//
//  Created by yryz on 2019/8/19.
//

#import <React/RCTView.h>
#import <React/RCTEventDispatcher.h>

#import "YdkQRCodeView.h"

@interface YdkQRCodeReactView : RCTView

@property (readonly, nonatomic, strong) YdkQRCodeView *qrCodeView;
@property (nonatomic, copy) RCTDirectEventBlock onResult;
@property (nonatomic, copy) RCTDirectEventBlock onError;

@property (nonatomic, copy) NSDictionary *config;

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;

@end
