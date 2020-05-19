//
//  YdkCommandsHandler.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkCommandsHandler.h"

#import "YdkRootViewController.h"
#import "React/RCTUIManager.h"
#import "YdkErrorHandler.h"
#import "React/RCTI18nUtil.h"
#import "UIViewController+LayoutProtocol.h"
#import "YdkLayoutManager.h"
#import "YdkNavigationOptions.h"

#import <ydk-toolkit/YdkToolkit.h>
#import <ydk-core/YdkAppDelegate.h>

NSString *const YdkNavigationDismissNotification = @"YdkNavigationDismissNotification";

static NSString* const setRoot  = @"setRoot";
static NSString* const push  = @"push";
static NSString* const pop  = @"pop";
static NSString* const popTo  = @"popTo";
static NSString* const popToRoot  = @"popToRoot";

@interface YdkCommandsHandler()

@property (nonatomic, strong) YdkControllerFactory *controllerFactory;
@property (nonatomic, strong) YdkNavigationStackManager *stackManager;
@property (nonatomic, strong) YdkEventEmitter *eventEmitter;
@property (nonatomic, strong) UIWindow *mainWindow;

@property (nonatomic, assign) BOOL processing;

@end

@implementation YdkCommandsHandler {
    
}

- (instancetype)initWithControllerFactory:(YdkControllerFactory*)controllerFactory eventEmitter:(YdkEventEmitter *)eventEmitter stackManager:(YdkNavigationStackManager *)stackManager mainWindow:(UIWindow *)mainWindow {
    self = [super init];
    _controllerFactory = controllerFactory;
    _eventEmitter = eventEmitter;
    _stackManager = stackManager;
    _mainWindow = mainWindow;
    return self;
}

#pragma mark - public

- (void)setRoot:(NSDictionary*)layout completion:(YdkTransitionCompletionBlock)completion {
    [self assertReady];
    //
    //  if (@available(iOS 9, *)) {
    //    if(_controllerFactory.defaultOptions.layout.direction.hasValue) {
    //      if ([_controllerFactory.defaultOptions.layout.direction.get isEqualToString:@"rtl"]) {
    //        [[RCTI18nUtil sharedInstance] allowRTL:YES];
    //        [[RCTI18nUtil sharedInstance] forceRTL:YES];
    //        [[UIView appearance] setSemanticContentAttribute:UISemanticContentAttributeForceRightToLeft];
    //        [[UINavigationBar appearance] setSemanticContentAttribute:UISemanticContentAttributeForceRightToLeft];
    //      } else {
    //        [[RCTI18nUtil sharedInstance] allowRTL:NO];
    //        [[RCTI18nUtil sharedInstance] forceRTL:NO];
    //        [[UIView appearance] setSemanticContentAttribute:UISemanticContentAttributeForceLeftToRight];
    //        [[UINavigationBar appearance] setSemanticContentAttribute:UISemanticContentAttributeForceLeftToRight];
    //      }
    //    }
    //  }
    //
    //  UIViewController *vc = [_controllerFactory createLayout:layout[@"root"]];
    //
    //  [vc renderTreeAndWait:[vc.resolveOptions.animations.setRoot.waitForRender getWithDefaultValue:NO] perform:^{
    //    _mainWindow.rootViewController = vc;
    //    [_eventEmitter sendOnNavigationCommandCompletion:setRoot commandId:commandId params:@{@"layout": layout}];
    //    completion() ;
    //  }];
}

- (void)push:(NSString*)componentId layout:(NSDictionary *)layout completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection {
    [self assertReady];
    
    NSString *desc = [NSString stringWithFormat:@"页面跳转失败 %@", layout.description];
    if (_processing) {
        rejection(@"500", @"页面跳转失败", [NSError errorWithDomain:@"YdkCommandsHandlerDomain" code:500 userInfo:@{ NSLocalizedDescriptionKey : desc }]);
        return;
    }
    _processing = YES;
    
    UIViewController *fromVC;
    if (!componentId) {
        fromVC = [UIViewController currentViewController];
        if (!fromVC.navigationController) {
            fromVC = fromVC.presentingViewController;
            if (!fromVC.navigationController && rejection) {
                rejection(@"404", @"未找到当前控制器", [NSError errorWithDomain:@"YdkCommandsHandlerDomain" code:404 userInfo:@{NSLocalizedDescriptionKey : @"未找到当前控制器"}]);
                _processing = NO;
                return;
            }
        }
    } else {
        fromVC = [YdkLayoutManager findComponentForId:componentId];
    }
    
//    let layout = {componentId, componentName, passProps}
    NSString *targetId = [layout objectForKey:@"componentId"];
    NSString *targetName = [layout objectForKey:@"componentName"];
    NSDictionary *passProps = [layout objectForKey:@"passProps"];
    NSDictionary *targetLayout = @{@"id" : targetId, @"data" : @{@"name" : targetName, @"passProps" : passProps ? : @{}}};
    UIViewController *newVc = [_controllerFactory createLayout:targetLayout];
    
    [newVc renderTreeAndWait:NO perform:^{ }];
    [self.stackManager push:newVc onTop:fromVC animated:YES animationDelegate:nil completion:^{
        self.processing = NO;
        completion();
    } rejection:^(NSString *code, NSString *message, NSError *error) {
        self.processing = NO;
        rejection(code, message, error);
    }];
    // 当未进入perform回调时，延时置为NO
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if (self.processing) self.processing = NO;
    });
}

- (void)pop:(NSString*)componentId completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection {
    [self assertReady];
    
    YdkRootViewController *vc = (YdkRootViewController*)[YdkLayoutManager findComponentForId:componentId];
    UINavigationController *nvc = vc.navigationController;
    
    if (vc && [nvc topViewController] == vc) {
        [_stackManager pop:vc animated:YES completion:^{
            completion();
        } rejection:rejection];
    } else {
        NSMutableArray * vcs = nvc.viewControllers.mutableCopy;
        [vcs removeObject:vc];
        [nvc setViewControllers:vcs animated:YES];
    }
}

- (void)popTo:(NSString*)componentId completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection {
    [self assertReady];
    YdkRootViewController *vc = (YdkRootViewController*)[YdkLayoutManager findComponentForId:componentId];
    //  RNNNavigationOptions *options = [[RNNNavigationOptions alloc] initWithDict:mergeOptions];
    //  [vc overrideOptions:options];
    [_stackManager popTo:vc animated:YES completion:^(NSArray *poppedViewControllers) {
        completion();
    } rejection:rejection];
}

- (void)popToRoot:(NSString*)componentId completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection {
    [self assertReady];
    YdkRootViewController *vc = (YdkRootViewController*)[YdkLayoutManager findComponentForId:componentId];
    //  RNNNavigationOptions *options = [[RNNNavigationOptions alloc] initWithDict:mergeOptions];
    //  [vc overrideOptions:options];
    [CATransaction begin];
    [CATransaction setCompletionBlock:^{
        completion();
    }];
    [_stackManager popToRoot:vc animated:YES completion:^(NSArray *poppedViewControllers) {
        
    } rejection:^(NSString *code, NSString *message, NSError *error) {
        
    }];
    [CATransaction commit];
}

- (void)showModal:(NSString *)componentId layout:(NSDictionary *)layout completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection {
    [self assertReady];
    
    NSString *desc = [NSString stringWithFormat:@"页面跳转失败 %@", layout.description];
    if (_processing) {
        rejection(@"500", @"页面跳转失败", [NSError errorWithDomain:@"YdkCommandsHandlerDomain" code:500 userInfo:@{ NSLocalizedDescriptionKey : desc }]);
        return;
    }
    _processing = YES;
    
    UIViewController *fromVC;
    if (!componentId) {
        fromVC = [UIViewController currentViewController];
        if (!fromVC.navigationController) {
            fromVC = fromVC.presentingViewController;
            if (!fromVC.navigationController && rejection) {
                rejection(@"404", @"未找到当前控制器", [NSError errorWithDomain:@"YdkCommandsHandlerDomain" code:404 userInfo:@{NSLocalizedDescriptionKey : @"未找到当前控制器"}]);
                self.processing = NO;
                return;
            }
        }
    } else {
        fromVC = [YdkLayoutManager findComponentForId:componentId];
    }
    
    //    let layout = {componentId, componentName, passProps}
    NSString *targetId = [layout objectForKey:@"componentId"];
    NSString *targetName = [layout objectForKey:@"componentName"];
    NSDictionary *passProps = [layout objectForKey:@"passProps"];
    NSDictionary *targetLayout = @{@"id" : targetId, @"data" : @{@"name" : targetName, @"passProps" : passProps ? : @{}}};
    UIViewController *newVc = [_controllerFactory createLayout:targetLayout];
    
    [newVc renderTreeAndWait:NO perform:^{ }];
    [self.stackManager showModal:newVc onTop:fromVC animated:YES completion:^{
        self.processing = NO;
        completion();
    } rejection:^(NSString *code, NSString *message, NSError *error) {
        self.processing = NO;
        rejection(code, message, error);
    }];
    // 当未进入perform回调时，延时置为NO
    dispatch_after(dispatch_time(DISPATCH_TIME_NOW, (int64_t)(2 * NSEC_PER_SEC)), dispatch_get_main_queue(), ^{
        if (self.processing) self.processing = NO;
    });
}

- (void)dismissModal:(NSString*)componentId completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection {
    [self assertReady];
    
    YdkRootViewController *vc = (YdkRootViewController*)[YdkLayoutManager findComponentForId:componentId];
    UINavigationController *nvc = vc.navigationController;
    
    if ([nvc topViewController] == vc) {
        
    } else {
        NSMutableArray * vcs = nvc.viewControllers.mutableCopy;
        [vcs removeObject:vc];
        [nvc setViewControllers:vcs animated:YES];
    }
    
    YdkRootViewController *bottomVC = nvc.viewControllers.firstObject;
    [_stackManager dismissModal:vc animated:YES completion:^{
        if (completion) completion();
        // 处理login取消事件
        [[NSNotificationCenter defaultCenter] postNotificationName:YdkNavigationDismissNotification object:bottomVC];
    } rejection:rejection];
}

- (void)setResult:(NSString *)componentId targetComponentId:(NSString *)targetComponentId data:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject {
    UIViewController *vc = [YdkLayoutManager findComponentForId:targetComponentId];
    SEL sel = @selector(setResult:data:);
    if (vc && [vc respondsToSelector:sel]) {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Warc-performSelector-leaks"
        // rn为了满足data为any类型，在调用原生时将参数用{data}包起来，针对业务处理只关心data中数据层，因为此处取出data中数据传给业务方处理
        [vc performSelector:sel withObject:componentId withObject:[data objectForKey:@"data"]];
#pragma clang diagnostic pop
    }
}

- (void)popToRootAndSwitchTab:(NSString *)componentId tabIndex:(NSInteger)tabIndex completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection {
    [self assertReady];
    [self popToRoot:componentId completion:^{
        UITabBarController *tabBar = (UITabBarController *)((YdkAppDelegate *)[UIApplication sharedApplication].delegate).window.rootViewController;
        [tabBar setSelectedIndex:tabIndex];
        completion();
    } rejection:rejection];
}

- (void)mergeOptions:(NSString*)componentId options:(NSDictionary*)mergeOptions completion:(YdkTransitionCompletionBlock)completion {
    [self assertReady];
    
    UIViewController<YdkLayoutProtocol>* vc = (UIViewController<YdkLayoutProtocol>*)[YdkLayoutManager findComponentForId:componentId];
    YdkNavigationOptions* newOptions = [[YdkNavigationOptions alloc] initWithDict:mergeOptions];
    [vc setOptions:newOptions];
    if ([vc conformsToProtocol:@protocol(YdkLayoutProtocol)] || [vc isKindOfClass:[YdkRootViewController class]]) {
        [CATransaction begin];
        [CATransaction setCompletionBlock:completion];
        [vc mergeOptions:newOptions];
        [CATransaction commit];
    }
}

#pragma mark - private

- (void)assertReady {
    if (!self.readyToReceiveCommands) {
        [[NSException exceptionWithName:@"BridgeNotLoadedError"
                                 reason:@"Bridge not yet loaded! Send commands after Navigation.events().onAppLaunched() has been called."
                               userInfo:nil]
         raise];
    }
}

@end
