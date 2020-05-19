//
//  YdkControllerFactory.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkControllerFactory.h"
#import "YdkLayoutNode.h"
#import "YdkLayoutInfo.h"
#import "YdkLayoutProtocol.h"
#import "YdkRootViewController.h"

@implementation YdkControllerFactory {
    id<YdkRootViewCreator> _creator;
    RCTBridge *_bridge;
    YdkReactComponentRegistry* _componentRegistry;
}

# pragma mark public

-(instancetype)initWithRootViewCreator:(id <YdkRootViewCreator>)creator
                          eventEmitter:(YdkEventEmitter*)eventEmitter
                     componentRegistry:(YdkReactComponentRegistry *)componentRegistry
                             andBridge:(RCTBridge*)bridge {
    
    self = [super init];
    
    _creator = creator;
    _eventEmitter = eventEmitter;
    _bridge = bridge;
    _componentRegistry = componentRegistry;
    
    return self;
}

- (UIViewController *)createLayout:(NSDictionary*)layout {
    UIViewController* layoutViewController = [self fromTree:layout];
    return layoutViewController;
}

# pragma mark private

- (UIViewController *)fromTree:(NSDictionary*)json {
    YdkLayoutNode* node = [YdkLayoutNode create:json];
    UIViewController *result = [self createComponent:node];;
    if (!result) {
        @throw [NSException exceptionWithName:@"UnknownControllerType" reason:[@"Unknown controller type " stringByAppendingString:node.type] userInfo:nil];
    }
    return result;
}

- (UIViewController *)createComponent:(YdkLayoutNode*)node {
    YdkLayoutInfo* layoutInfo = [[YdkLayoutInfo alloc] initWithNode:node];
    UIViewController *component;
    if (node.type) {
        Class nodeClass = NSClassFromString(node.type);
        component = [[nodeClass alloc] initWithLayoutInfo:layoutInfo creator:_creator eventEmitter:_eventEmitter];
    } else {
        component = [[YdkRootViewController alloc] initWithLayoutInfo:layoutInfo creator:_creator eventEmitter:_eventEmitter];
    }
    return component;
}

@end
