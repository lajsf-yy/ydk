//
//  YdkQRCodeViewManager.m
//  ydk-qrcode
//
//  Created by yryz on 2019/8/19.
//

#import "YdkQRCodeViewManager.h"
#import "YdkQRCodeReactView.h"

#import <React/RCTUIManager.h>

@implementation YdkQRCodeViewManager

RCT_EXPORT_MODULE(YdkScannerView)

- (UIView *)view {
    return [[YdkQRCodeReactView alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
}

RCT_EXPORT_VIEW_PROPERTY(config, NSDictionary)
RCT_EXPORT_VIEW_PROPERTY(onResult, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onError, RCTDirectEventBlock)

RCT_EXPORT_METHOD(setFlashLight:(nonnull NSNumber *)reactTag)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, YdkQRCodeReactView *> *viewRegistry) {
        YdkQRCodeReactView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[YdkQRCodeReactView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting YdkQRCodeReactView, got: %@", view);
        } else {
            [view.qrCodeView flashlighAction];
        }
    }];
}

@end
