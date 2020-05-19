//
//  Ydkavigation.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkNavigation.h"
#import <stdatomic.h>

#import <React/RCTUIManager.h>
#import <React/RCTBundleURLProvider.h>

#import "YdkBridgeManager.h"
#import "YdkLayoutManager.h"

@interface YdkNavigation() <YdkBridgeManagerDelegate>

@property (nonatomic, strong) YdkBridgeManager *bridgeManager;
@property (nonatomic, strong) id<YdkNativeComponentRouteProtocol> handler;
@property (nonatomic, copy) void (^onJavaScriptLoadedBlock)(NSError *error);

@end

@implementation YdkNavigation

# pragma mark - public API

+ (void)bootstrap:(NSDictionary *)launchOptions jsCodeLocation:(NSURL *)jsCodeLocation mainWindow:(UIWindow *)mainWindow {
    [[YdkNavigation sharedInstance] bootstrap:launchOptions jsCodeLocation:jsCodeLocation mainWindow:mainWindow];
}

+ (void)registerNativeComponentRouteHandler:(id<YdkNativeComponentRouteProtocol>)handler {
    [[YdkNavigation sharedInstance] registerNativeComponentRouteHandler:handler];
}

+ (RCTBridge *)getBridge {
  return [[YdkNavigation sharedInstance].bridgeManager bridge];
}

+ (UIViewController *)findViewController:(NSString *)componentId {
  return [YdkLayoutManager findComponentForId:componentId];
}

# pragma mark - instance

+ (instancetype)sharedInstance {
  static YdkNavigation *instance = nil;
  static dispatch_once_t onceToken = 0;
  dispatch_once(&onceToken,^{
    if (instance == nil) {
      instance = [[YdkNavigation alloc] init];
    }
  });
  
  return instance;
}

- (void)bootstrap:(NSDictionary *)launchOptions jsCodeLocation:(NSURL *)jsCodeLocation mainWindow:(UIWindow *)mainWindow {
  self.bridgeManager = [[YdkBridgeManager alloc] initWithJsCodeLocation:jsCodeLocation launchOptions:launchOptions bridgeManagerDelegate:self mainWindow:mainWindow];
}

- (void)registerNativeComponentRouteHandler:(id<YdkNativeComponentRouteProtocol>)handler {
    _handler = handler;
}

- (void)route:(NSString *)name passProps:(NSDictionary *)passProps completion:(RCTPromiseResolveBlock)completion rejection:(RCTPromiseRejectBlock)rejection {
    if (![name isKindOfClass:[NSString class]] || !name.length) {
        NSError *error = [NSError errorWithDomain:@"YdkNavigationErrorDomain" code:-1000 userInfo:@{NSLocalizedDescriptionKey : @"无效的moduleName"}];
        if (rejection) {
            rejection(@(error.code).stringValue, error.userInfo[NSLocalizedDescriptionKey], error);
        }
        return;
    }
    
    if (!_bridgeManager.commandsHandler.readyToReceiveCommands) {
        if (self.onJavaScriptLoadedBlock) {
            NSError *error = [NSError errorWithDomain:@"YdkNavigationDomain" code:500 userInfo:@{NSLocalizedDescriptionKey : @"覆盖当前路由操作"}];
            self.onJavaScriptLoadedBlock(error);
        } else {
            __weak __typeof(&*self) weakSelf = self;
            self.onJavaScriptLoadedBlock = ^(NSError *error) {
                if (error) {
                    if (rejection) {
                        rejection(@(error.code).stringValue, error.userInfo[NSLocalizedDescriptionKey], error);
                    }
                } else {
                    __strong __typeof(&*weakSelf) strongSelf = weakSelf;
                    [strongSelf route:name passProps:passProps completion:completion rejection:rejection];
                }
            };
        }
        return;
    }
    if ([_bridgeManager.bridgeModule isRNCompment:name]) {
        NSString *componentId = [YdkNavigation createUniqueComponentId];
        // like YdkLayoutInfo
        NSDictionary *layout = @{@"componentId" : componentId, @"componentName" : name, @"passProps" : passProps ? : @{}};
        [_bridgeManager.commandsHandler push:nil layout:layout completion:^{
            if (completion) completion(nil);
        } rejection:rejection];
    } else {
        [self routeNativeComponent:name passProps:passProps resolver:completion rejecter:rejection];
    }
}

- (void)setResult:(NSString *)componentId targetComponentId:(NSString *)targetComponentId data:(NSDictionary *)data {
    [self.bridgeManager.commandsHandler setResult:componentId targetComponentId:targetComponentId data:data resolver:nil rejecter:nil];
}

// MARK: - Class Method
static atomic_int ID = 1;
+ (NSString *)createUniqueComponentId {
    atomic_int componentId = atomic_fetch_add(&ID, 1);
    return [NSString stringWithFormat:@"nativeComponent%i", componentId];
}

+ (void)route:(NSString *)name completion:(RCTPromiseResolveBlock)completion rejection:(RCTPromiseRejectBlock)rejection {
    [self route:name passProps:nil completion:completion rejection:rejection];
}

+ (void)route:(NSString *)name passProps:(NSDictionary *)passProps completion:(RCTPromiseResolveBlock)completion rejection:(RCTPromiseRejectBlock)rejection {
    [[self sharedInstance] route:name passProps:passProps completion:completion rejection:rejection];
}

// MARK: - YdkBridgeManagerDelegate
- (void)onJavaScriptLoaded {
    if (_onJavaScriptLoadedBlock) {
        _onJavaScriptLoadedBlock(nil);
        _onJavaScriptLoadedBlock = nil;
    }
}

- (void)routeNativeComponent:(NSString *)componentName passProps:(NSDictionary *)passProps resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    NSAssert(_handler, @"请调用类方法registerNativeComponentRouteHandler注册原生组件路由处理");
    [_handler routeNativeComponent:componentName passProps:passProps resolver:resolve rejecter:reject];
}

// MARK: - Helps
+ (YdkRootViewController *)createRootViewController:(NSString *)name {
    return [self createRootViewController:name passProps:nil];
}

+ (YdkRootViewController *)createRootViewController:(NSString *)name passProps:(NSDictionary *)passProps {
    return [self createRootViewController:name passProps:passProps renderTree:YES];
}

+ (YdkRootViewController *)createRootViewController:(NSString *)name passProps:(NSDictionary *)passProps renderTree:(BOOL)renderTree {
    return [self createRootViewController:name passProps:passProps type:nil renderTree:renderTree];
}

+ (YdkRootViewController *)createRootViewController:(NSString *)name passProps:(NSDictionary *)passProps type:(NSString *)type renderTree:(BOOL)renderTree {
    NSString *componentId = [YdkNavigation createUniqueComponentId];
    // like YdkLayoutNode
    NSDictionary *layout = @{@"id" : componentId,
                             @"data" : @{@"name" : name, @"passProps" : passProps ? : @{}},
                             @"type" : type ? : @"YdkRootViewController" };
    YdkRootViewController *rootVC = (YdkRootViewController *)[[YdkNavigation sharedInstance].bridgeManager.controllerFactory createLayout:layout];
    if (renderTree) {
        [rootVC renderTreeAndWait:NO perform:^{
            
        }];
    }
    return rootVC;
}

+ (void)setResult:(NSString *)componentId targetComponentId:(NSString *)targetComponentId data:(id)data {
    [[self sharedInstance] setResult:componentId targetComponentId:targetComponentId data:@{@"data" : data ? : [NSNull null]}];
}

@end
