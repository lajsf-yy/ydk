//
//  YdkTrackerModule.m
//  ydk-track
//
//  Created by yryz on 2019/6/20.
//

#import "YdkTrackerModule.h"

@implementation YdkTrackerModule

RCT_EXPORT_MODULE(YdkTrackModule)

RCT_EXPORT_METHOD(setEvent:(NSString *)eventName eventData:(NSDictionary *)eventData resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    [YdkTracker event:eventName attributes:eventData];
    resolver(nil);
}

RCT_EXPORT_METHOD(startTrack:(NSString *)eventName resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    [YdkTracker startTrack:eventName];
    resolver(nil);
}

RCT_EXPORT_METHOD(endTrack:(NSString *)eventName eventData:(NSDictionary *)eventData resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    [YdkTracker endTrack:eventName eventData:eventData];
    resolver(nil);
}

RCT_EXPORT_METHOD(identify:(NSString *)uid eventData:(NSDictionary *)eventData resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    [YdkTracker identify:uid eventData:eventData];
    resolver(nil);
}

@end
