//
//  YdkReactView.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkReactView.h"
#import "RCTHelpers.h"
#import <React/RCTUIManager.h>

@implementation YdkReactView

- (instancetype)initWithBridge:(RCTBridge *)bridge moduleName:(NSString *)moduleName initialProperties:(NSDictionary *)initialProperties availableSize:(CGSize)availableSize reactViewReadyBlock:(YdkReactViewReadyCompletionBlock)reactViewReadyBlock {
    self = [super initWithBridge:bridge moduleName:moduleName initialProperties:initialProperties];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(contentDidAppear:) name:RCTContentDidAppearNotification object:nil];
    _reactViewReadyBlock = reactViewReadyBlock;
    [bridge.uiManager setAvailableSize:availableSize forRootView:self];
    self.backgroundColor = [UIColor clearColor];
    return self;
}

- (void)contentDidAppear:(NSNotification *)notification {
#ifdef DEBUG
    if ([((YdkReactView *)notification.object).moduleName isEqualToString:self.moduleName]) {
        [RCTHelpers removeYellowBox:self];
    }
#endif
    YdkReactView* appearedView = notification.object;
    if (_reactViewReadyBlock && [appearedView.appProperties[@"componentId"] isEqual:self.appProperties[@"componentId"]]) {
      _reactViewReadyBlock();
      _reactViewReadyBlock = nil;
      [[NSNotificationCenter defaultCenter] removeObserver:self];
    }
}

- (void)setRootViewDidChangeIntrinsicSize:(void (^)(CGSize))rootViewDidChangeIntrinsicSize {
    _rootViewDidChangeIntrinsicSize = rootViewDidChangeIntrinsicSize;
    self.delegate = self;
}

- (void)rootViewDidChangeIntrinsicSize:(RCTRootView *)rootView {
    if (_rootViewDidChangeIntrinsicSize) {
        _rootViewDidChangeIntrinsicSize(rootView.intrinsicContentSize);
    }
}

- (void)setAlignment:(NSString *)alignment inFrame:(CGRect)frame {
    if ([alignment isEqualToString:@"fill"]) {
        self.sizeFlexibility = RCTRootViewSizeFlexibilityNone;
        [self setFrame:frame];
    } else {
        self.sizeFlexibility = RCTRootViewSizeFlexibilityWidthAndHeight;
        __weak YdkReactView *weakSelf = self;
        [self setRootViewDidChangeIntrinsicSize:^(CGSize intrinsicSize) {
            [weakSelf setFrame:CGRectMake(0, 0, intrinsicSize.width, intrinsicSize.height)];
        }];
    }
}

@end
