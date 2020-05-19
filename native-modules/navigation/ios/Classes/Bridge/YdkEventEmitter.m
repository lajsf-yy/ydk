//
//  YdkEventEmitter.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkEventEmitter.h"

@implementation YdkEventEmitter
{
    BOOL _hasListeners;
}

RCT_EXPORT_MODULE(YdkNavigationEventEmitter);

static NSString* const ComponentDidAppear    = @"ComponentDidAppear";
static NSString* const ComponentDidDisappear  = @"ComponentDidDisappear";
static NSString* const ComponentReceiveResult  = @"ComponentReceiveResult";

-(NSArray<NSString *> *)supportedEvents {
  return @[ComponentDidAppear,
           ComponentDidDisappear,
           ComponentReceiveResult
           ];
}

- (void)startObserving {
    _hasListeners = YES;
}

- (void)stopObserving {
    _hasListeners = NO;
}

- (void)sendEventWithName:(NSString *)name body:(id)body {
    if (_hasListeners) {
        [super sendEventWithName:name body:body];
    }
}

-(void)sendComponentDidAppear:(NSString *)componentId componentName:(NSString *)componentName {
  [self send:ComponentDidAppear body:@{
                                       @"componentId":componentId,
                                       @"componentName": componentName
                                       }];
}

-(void)sendComponentDidDisappear:(NSString *)componentId componentName:(NSString *)componentName{
  [self send:ComponentDidDisappear body:@{
                                          @"componentId":componentId,
                                          @"componentName": componentName
                                          }];
}

-(void)sendComponentReceiveResult:(NSString *)componentId data:(id)data {
    [self send:ComponentReceiveResult body:@{
                                             @"componentId" : componentId ? : [NSNull null],
                                             @"data": @{ @"data" : data ? : [NSNull null] }
                                             }];
}

-(void)send:(NSString *)eventName body:(id)body {
  if (self.bridge == nil) {
    return;
  }
  [self sendEventWithName:eventName body:body];
}

@end
