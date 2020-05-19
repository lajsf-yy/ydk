//
//  YdkReactView.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <React/RCTRootView.h>
#import <React/RCTRootViewDelegate.h>

typedef void (^YdkReactViewReadyCompletionBlock)(void);

@interface YdkReactView : RCTRootView <RCTRootViewDelegate>

- (instancetype)initWithBridge:(RCTBridge *)bridge moduleName:(NSString *)moduleName initialProperties:(NSDictionary *)initialProperties availableSize:(CGSize)availableSize reactViewReadyBlock:(YdkReactViewReadyCompletionBlock)reactViewReadyBlock;

@property (nonatomic, copy) void (^rootViewDidChangeIntrinsicSize)(CGSize intrinsicSize);
@property (nonatomic, copy) YdkReactViewReadyCompletionBlock reactViewReadyBlock;

- (void)setAlignment:(NSString *)alignment inFrame:(CGRect)frame;

@end
