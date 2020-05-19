//
//  YdkControllerFactory.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "YdkRootViewCreator.h"
#import "YdkEventEmitter.h"
#import "YdkReactComponentRegistry.h"

@interface YdkControllerFactory : NSObject

-(instancetype)initWithRootViewCreator:(id <YdkRootViewCreator>)creator
                          eventEmitter:(YdkEventEmitter*)eventEmitter
                     componentRegistry:(YdkReactComponentRegistry *)componentRegistry
                             andBridge:(RCTBridge*)bridge;

- (UIViewController *)createLayout:(NSDictionary*)layout;

@property (nonatomic, strong) YdkEventEmitter *eventEmitter;

@end
