//
//  YdkReactRootViewCreator.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkReactRootViewCreator.h"
#import "YdkReactView.h"
#import "YdkComponentOptions.h"

@implementation YdkReactRootViewCreator {
  RCTBridge *_bridge;
}

- (instancetype)initWithBridge:(RCTBridge*)bridge {
  self = [super init];
  
  _bridge = bridge;
  
  return self;
}

- (YdkReactView *)createRootView:(NSString*)name initialProperties:(NSDictionary *)initialProperties availableSize:(CGSize)availableSize reactViewReadyBlock:(YdkReactViewReadyCompletionBlock)reactViewReadyBlock {
  if (![initialProperties objectForKey:@"componentId"]) {
    @throw [NSException exceptionWithName:@"MissingViewId" reason:@"Missing view id" userInfo:nil];
  }
  
  YdkReactView *view = [[YdkReactView alloc] initWithBridge:_bridge
                                                 moduleName:name
                                          initialProperties:initialProperties
                                              availableSize:availableSize
                                        reactViewReadyBlock:reactViewReadyBlock];
  return view;
}

- (UIView*)createRootViewFromComponentOptions:(YdkComponentOptions *)componentOptions {
    NSDictionary *props = @{@"componentId" : componentOptions.componentId,
                            @"name" : componentOptions.name
                            };
  return [self createRootView:componentOptions.name initialProperties:props availableSize:CGSizeZero reactViewReadyBlock:nil];
}

- (UIView*)createRootViewFromComponentOptions:(YdkComponentOptions *)componentOptions reactViewReadyBlock:(YdkReactViewReadyCompletionBlock)reactViewReadyBlock {
    NSDictionary *props = @{@"componentId" : componentOptions.componentId,
                            @"name" : componentOptions.name
                            };
  return [self createRootView:componentOptions.name initialProperties:props availableSize:CGSizeZero reactViewReadyBlock:reactViewReadyBlock];
}

@end
