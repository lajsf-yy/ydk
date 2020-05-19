//
//  YdkBridgeModule.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

#import "YdkCommandsHandler.h"

@protocol YdkNativeComponentRouteProtocol;
@interface YdkBridgeModule : NSObject <RCTBridgeModule>

@property (readonly, nonatomic, strong) YdkCommandsHandler *commandsHandler;
@property (nonatomic, weak) id<YdkNativeComponentRouteProtocol> delegate;

- (instancetype)initWithCommandsHandler:(YdkCommandsHandler *)commandsHandler;
- (BOOL)isRNCompment:(NSString *)componentName;

@end
