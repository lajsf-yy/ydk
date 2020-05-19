//
//  YdkTracker.m
//  ydk-track
//
//  Created by yryz on 2019/6/20.
//

#import "YdkTracker.h"

#import <Zhugeio/Zhuge.h>

@implementation YdkTracker
{
    NSString *_appKey;
}

+ (void)load {
    ydk_register_module(self);
}

- (instancetype)initWithConfig:(NSDictionary *)config {
    self = [super init];
    if (self) {
        _appKey = [config objectForKey:@"zhugeAppKey"];
    }
    return self;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    // [[Zhuge sharedInstance].config setDebug:YES];
    [[Zhuge sharedInstance] startWithAppKey:_appKey launchOptions:launchOptions];
    return YES;
}

+ (void)event:(NSString *)eventId attributes:(NSDictionary *)attributes {
    [[Zhuge sharedInstance] track:eventId properties:attributes];
}

+ (void)startTrack:(NSString *)eventName {
    [[Zhuge sharedInstance] startTrack:eventName];
}

+ (void)endTrack:(NSString *)eventName eventData:(NSDictionary *)eventData {
    [[Zhuge sharedInstance] endTrack:eventName properties:eventData];
}

+ (void)identify:(NSString *)userId eventData:(NSDictionary *)eventData {
    [[Zhuge sharedInstance] identify:userId properties:eventData];
}

@end
