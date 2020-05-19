//
//  YdkReactComponentRegistry.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "YdkReactView.h"
#import "YdkRootViewCreator.h"
#import "YdkComponentOptions.h"

@interface YdkReactComponentRegistry : NSObject

- (instancetype)initWithCreator:(id<YdkRootViewCreator>)creator;

- (YdkReactView *)createComponentIfNotExists:(YdkComponentOptions *)component parentComponentId:(NSString *)parentComponentId reactViewReadyBlock:(YdkReactViewReadyCompletionBlock)reactViewReadyBlock;

- (void)removeComponent:(NSString *)componentId;

- (void)clearComponentsForParentId:(NSString *)parentComponentId;

- (void)clear;

@end
