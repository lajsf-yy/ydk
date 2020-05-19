//
//  YdkLocationModule.m
//  ydk-location
//
//  Created by yryz on 2019/7/25.
//

#import "YdkLocationModule.h"
#import "YdkLocation.h"
#import "YdkLocationInfo.h"

@implementation YdkLocationModule

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_MODULE()

RCT_EXPORT_METHOD(getCurrentLocation:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    @weakify(self);
    [[YdkLocation requestLocation] subscribeNext:^(YdkLocationInfo *x) {
        @strongify(self);
        resolver([self locationInfoJSON:x]);
    } error:^(NSError *error) {
        rejecter(@(error.code).stringValue, [error.userInfo objectForKey:NSLocalizedDescriptionKey], error);
    }];
}

- (NSDictionary *)locationInfoJSON:(YdkLocationInfo *)info {
    NSMutableDictionary *loc = [NSMutableDictionary dictionary];
    [loc setObject:@(info.coordinate.latitude) forKey:@"latitude"];
    [loc setObject:@(info.coordinate.longitude) forKey:@"longitude"];
    if (info.province) [loc setObject:info.province forKey:@"provinceName"];
    if (info.city) [loc setObject:info.city forKey:@"cityName"];
    if (info.region) [loc setObject:info.region forKey:@"regionName"];
    if (info.address) [loc setObject:info.address forKey:@"addressName"];
    if (info.name) [loc setObject:info.name forKey:@"name"];
    if (info.citycode) [loc setObject:info.citycode forKey:@"cityCode"];
    if (info.adcode) [loc setObject:info.adcode forKey:@"adCode"];
    return loc;
}

@end
