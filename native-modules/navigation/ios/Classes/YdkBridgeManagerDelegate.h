//
//  YdkBridgeManagerDelegate.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <React/RCTBridgeModule.h>

@protocol YdkNativeComponentRouteProtocol <NSObject>

- (void)routeNativeComponent:(NSString *)componentName passProps:(NSDictionary *)passProps resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;

@end

@protocol YdkBridgeManagerDelegate <YdkNativeComponentRouteProtocol>

@optional

- (NSArray<id<RCTBridgeModule>> *)extraModulesForBridge:(RCTBridge *)bridge;

- (void)onJavaScriptWillLoad;

- (void)onJavaScriptLoaded;

@end



