//
//  YdkRootViewCreator.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkReactView.h"
#import "YdkComponentOptions.h"

@protocol YdkRootViewCreator

- (YdkReactView*)createRootView:(NSString*)name initialProperties:(NSDictionary *)initialProperties availableSize:(CGSize)availableSize reactViewReadyBlock:(YdkReactViewReadyCompletionBlock)reactViewReadyBlock;

- (UIView*)createRootViewFromComponentOptions:(YdkComponentOptions *)componentOptions;

- (UIView*)createRootViewFromComponentOptions:(YdkComponentOptions *)componentOptions reactViewReadyBlock:(YdkReactViewReadyCompletionBlock)reactViewReadyBlock;

@end
