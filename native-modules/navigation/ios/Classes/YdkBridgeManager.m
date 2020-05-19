//
//  YdkBridgeManager.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkBridgeManager.h"

#import <React/RCTBridge.h>
#import <React/RCTUIManager.h>

#import "YdkEventEmitter.h"
#import "YdkRootViewCreator.h"
#import "YdkReactRootViewCreator.h"
#import "YdkReactComponentRegistry.h"
#import <React/RCTDevLoadingView.h>

@interface YdkBridgeManager() <RCTBridgeDelegate>

@property (readwrite) RCTBridge *bridge;
@property (readwrite) YdkBridgeModule *bridgeModule;
@property (readwrite) YdkCommandsHandler *commandsHandler;
@property (readwrite) YdkControllerFactory *controllerFactory;

@property (nonatomic, strong) YdkReactComponentRegistry *componentRegistry;

@end

@implementation YdkBridgeManager {
    NSURL* _jsCodeLocation;
    NSDictionary* _launchOptions;
    id<YdkBridgeManagerDelegate> _delegate;
    UIWindow* _mainWindow;
}

- (instancetype)initWithJsCodeLocation:(NSURL *)jsCodeLocation launchOptions:(NSDictionary *)launchOptions bridgeManagerDelegate:(id<YdkBridgeManagerDelegate>)delegate mainWindow:(UIWindow *)mainWindow {
    if (self = [super init]) {
        _mainWindow = mainWindow;
        _jsCodeLocation = jsCodeLocation;
        _launchOptions = launchOptions;
        _delegate = delegate;
        
        _bridge = [[RCTBridge alloc] initWithDelegate:self launchOptions:_launchOptions];
        
#if RCT_DEV
        [_bridge moduleForClass:[RCTDevLoadingView class]];
#endif
        
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(onJavaScriptLoaded)
                                                     name:RCTJavaScriptDidLoadNotification
                                                   object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(onJavaScriptWillLoad)
                                                     name:RCTJavaScriptWillStartLoadingNotification
                                                   object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self
                                                 selector:@selector(onBridgeWillReload)
                                                     name:RCTBridgeWillReloadNotification
                                                   object:nil];
    }
    return self;
}

- (NSArray *)extraModulesFromDelegate {
    if ([_delegate respondsToSelector:@selector(extraModulesForBridge:)]) {
        return [_delegate extraModulesForBridge:_bridge];
    }
    return nil;
}

# pragma mark - RCTBridgeDelegate

- (NSURL *)sourceURLForBridge:(RCTBridge *)bridge {
    return _jsCodeLocation;
}

- (NSArray<id<RCTBridgeModule>> *)extraModulesForBridge:(RCTBridge *)bridge {
    YdkEventEmitter *eventEmitter = [[YdkEventEmitter alloc] init];
    
    id<YdkRootViewCreator> rootViewCreator = [[YdkReactRootViewCreator alloc] initWithBridge:bridge];
    _componentRegistry = [[YdkReactComponentRegistry alloc] initWithCreator:rootViewCreator];
    _controllerFactory = [[YdkControllerFactory alloc] initWithRootViewCreator:rootViewCreator eventEmitter:eventEmitter componentRegistry:_componentRegistry andBridge:bridge];
    
    _commandsHandler = [[YdkCommandsHandler alloc] initWithControllerFactory:_controllerFactory eventEmitter:eventEmitter stackManager:[YdkNavigationStackManager new] mainWindow:_mainWindow];
    YdkBridgeModule *bridgeModule = [[YdkBridgeModule alloc] initWithCommandsHandler:_commandsHandler];
    bridgeModule.delegate = _delegate;
    _bridgeModule = bridgeModule;
    return [@[bridgeModule, eventEmitter] arrayByAddingObjectsFromArray:[self extraModulesFromDelegate]];
}

# pragma mark - JavaScript & Bridge Notifications

- (void)onJavaScriptWillLoad {
    [_componentRegistry clear];
    if ([_delegate respondsToSelector:@selector(onJavaScriptWillLoad)]) {
        [_delegate onJavaScriptWillLoad];
    }
}

- (void)onJavaScriptLoaded {
    [_commandsHandler setReadyToReceiveCommands:true];
    //  [[_bridge moduleForClass:[YdkEventEmitter class]] sendOnAppLaunched];
    if ([_delegate respondsToSelector:@selector(onJavaScriptLoaded)]) {
        [_delegate onJavaScriptLoaded];
    }
}

- (void)onBridgeWillReload {
    //  UIApplication.sharedApplication.delegate.window.rootViewController =  nil;
}

@end
