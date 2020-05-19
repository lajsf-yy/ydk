//
//  Ydkavigation.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <React/RCTBridge.h>
#import <React/RCTUIManager.h>
#import "YdkBridgeManagerDelegate.h"
#import "UIViewController+LayoutProtocol.h"
#import "YdkCommandsHandler.h"
#import "YdkControllerFactory.h"
#import "YdkRootViewController.h"
#import "YdkNavigationResultProtocol.h"

FOUNDATION_EXTERN NSString *const YdkNavigationDismissNotification;

/**
 管理react-native导航跳转
 */
@interface YdkNavigation : NSObject

+ (instancetype)sharedInstance;

+ (void)bootstrap:(NSDictionary *)launchOptions jsCodeLocation:(NSURL *)jsCodeLocation mainWindow:(UIWindow *)mainWindow;

+ (void)registerNativeComponentRouteHandler:(id<YdkNativeComponentRouteProtocol>)handler;

+ (UIViewController *)findViewController:(NSString *)componentId;

+ (RCTBridge *)getBridge;

+ (NSString *)createUniqueComponentId;

/**
 跳由一个rn页面

 @param name rn页面注册的name
 @param completion 完成回调
 @param rejection 失败回调
 */
+ (void)route:(NSString *)name completion:(RCTPromiseResolveBlock)completion rejection:(RCTPromiseRejectBlock)rejection;
+ (void)route:(NSString *)name passProps:(NSDictionary *)passProps completion:(RCTPromiseResolveBlock)completion rejection:(RCTPromiseRejectBlock)rejection;

+ (YdkRootViewController *)createRootViewController:(NSString *)name;
+ (YdkRootViewController *)createRootViewController:(NSString *)name passProps:(NSDictionary *)passProps;
+ (YdkRootViewController *)createRootViewController:(NSString *)name passProps:(NSDictionary *)passProps renderTree:(BOOL)renderTree;

/**
创建一个rn页面控制器

@param name rn页面注册的name
@param passProps rn页面props
@param renderTree 渲染视图树
@param type 控制器类名，必须为YdkRootViewController子类
@return 包含rn页面的控制器
*/
+ (YdkRootViewController *)createRootViewController:(NSString *)name passProps:(NSDictionary *)passProps type:(NSString *)type renderTree:(BOOL)renderTree;

/**
 回调某页面传值

 @param componentId 当前页面componentId
 @param targetComponentId 目标页面componentId
 @param data 传值数据
 */
+ (void)setResult:(NSString *)componentId targetComponentId:(NSString *)targetComponentId data:(id)data;

@end

