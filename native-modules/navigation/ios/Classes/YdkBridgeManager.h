//
//  YdkBridgeManager.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <React/RCTBridge.h>
#import "YdkBridgeManagerDelegate.h"
#import "YdkBridgeModule.h"
#import "YdkCommandsHandler.h"
#import "YdkControllerFactory.h"

@interface YdkBridgeManager : NSObject <RCTBridgeDelegate>

- (instancetype)initWithJsCodeLocation:(NSURL *)jsCodeLocation launchOptions:(NSDictionary *)launchOptions bridgeManagerDelegate:(id<YdkBridgeManagerDelegate>)delegate mainWindow:(UIWindow *)mainWindow;

@property (readonly, nonatomic, strong) RCTBridge *bridge;
@property (readonly, nonatomic, strong) YdkBridgeModule *bridgeModule;
@property (readonly, nonatomic, strong) YdkCommandsHandler *commandsHandler;
@property (readonly, nonatomic, strong) YdkControllerFactory *controllerFactory;

@end
