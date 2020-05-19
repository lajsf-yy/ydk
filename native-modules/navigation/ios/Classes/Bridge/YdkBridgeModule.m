//
//  YdkBridgeModule.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkBridgeModule.h"
#import "YdkNavigationConstants.h"
#import "YdkLayoutManager.h"
#import "YdkNavigationResultProtocol.h"
#import "YdkBridgeManagerDelegate.h"

@interface YdkBridgeModule ()

@property (nonatomic, strong) NSMutableSet *components;

@end

@implementation YdkBridgeModule 
@synthesize bridge = _bridge;
RCT_EXPORT_MODULE(YdkNavigationModule);

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

- (instancetype)initWithCommandsHandler:(YdkCommandsHandler *)commandsHandler {
    self = [super init];
    _commandsHandler = commandsHandler;
    _components = [NSMutableSet set];
    return self;
}

- (BOOL)isRNCompment:(NSString *)componentName {
    return [_components containsObject:componentName];
}

#pragma mark - JS interface
RCT_EXPORT_METHOD(registerComponents:(NSArray<NSString *> *)components  resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [_components addObjectsFromArray:components];
    resolve(nil);
}

RCT_EXPORT_METHOD(setRoot:(NSString*)commandId layout:(NSDictionary*)layout resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler setRoot:layout completion:^{
        resolve(layout);
    }];
}

RCT_EXPORT_METHOD(push:(NSString*)componentId layout:(NSDictionary*)layout resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    NSString *componentName = [layout objectForKey:@"componentName"];
    if ([_components containsObject:componentName]) {
        [_commandsHandler push:componentId layout:layout completion:^{
            resolve(componentId);
        } rejection:reject];
    } else {
        NSDictionary *passProps = [layout objectForKey:@"passProps"];
        [_delegate routeNativeComponent:componentName passProps:passProps resolver:resolve rejecter:reject];
    }
}

RCT_EXPORT_METHOD(pop:(NSString*)componentId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler pop:componentId completion:^{
        resolve(componentId);
    } rejection:reject];
}

RCT_EXPORT_METHOD(popTo:(NSString*)componentId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler popTo:componentId completion:^{
        resolve(componentId);
    } rejection:reject];
}

RCT_EXPORT_METHOD(popToRoot:(NSString*)componentId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler popToRoot:componentId completion:^{
        resolve(componentId);
    } rejection:reject];
}

RCT_EXPORT_METHOD(getConstants:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    resolve([YdkNavigationConstants getConstants]);
}

RCT_EXPORT_METHOD(showModal:(NSString*)componentId layout:(NSDictionary*)layout resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler showModal:componentId layout:layout completion:^{
        resolve(componentId);
    } rejection:reject];
}

RCT_EXPORT_METHOD(dismissModal:(NSString*)componentId resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler dismissModal:componentId completion:^{
        resolve(componentId);
    } rejection:reject];
}

RCT_EXPORT_METHOD(setResult:(NSString *)componentId targetComponentId:(NSString *)targetComponentId data:(NSDictionary *)data resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler setResult:componentId targetComponentId:targetComponentId data:data resolver:resolve rejecter:reject];
}

RCT_EXPORT_METHOD(popToRootAndSwitchTab:(NSString *)componentId tabIndex:(NSInteger)tabIndex resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler popToRootAndSwitchTab:componentId tabIndex:tabIndex completion:^{
        resolve(nil);
    } rejection:reject];
}

RCT_EXPORT_METHOD(mergeOptions:(NSString*)componentId options:(NSDictionary*)options resolver:(RCTPromiseResolveBlock)resolve rejecter:(RCTPromiseRejectBlock)reject) {
    [_commandsHandler mergeOptions:componentId options:options completion:^{
        resolve(componentId);
    }];
}

@end

