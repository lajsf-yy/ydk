//
//  YdkCommandsHandler.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "YdkControllerFactory.h"
#import "YdkNavigationStackManager.h"

@interface YdkCommandsHandler : NSObject

- (instancetype)initWithControllerFactory:(YdkControllerFactory*)controllerFactory eventEmitter:(YdkEventEmitter *)eventEmitter stackManager:(YdkNavigationStackManager *)stackManager mainWindow:(UIWindow *)mainWindow;

@property (nonatomic) BOOL readyToReceiveCommands;

- (void)setRoot:(NSDictionary*)layout completion:(YdkTransitionCompletionBlock)completion;

- (void)push:(NSString*)componentId layout:(NSDictionary *)layout completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection;

- (void)pop:(NSString*)componentId completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection;

- (void)popTo:(NSString*)componentId completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection;

- (void)popToRoot:(NSString*)componentId completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection;

- (void)showModal:(NSString *)componentId layout:(NSDictionary *)layout completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection;

- (void)dismissModal:(NSString*)componentId completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection;

- (void)setResult:(NSString *)componentId targetComponentId:(NSString *)targetComponentId data:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject;

- (void)popToRootAndSwitchTab:(NSString *)componentId tabIndex:(NSInteger)tabIndex completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection;

- (void)mergeOptions:(NSString*)componentId options:(NSDictionary*)mergeOptions completion:(YdkTransitionCompletionBlock)completion;

@end
