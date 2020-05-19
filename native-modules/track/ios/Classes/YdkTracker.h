//
//  YdkTracker.h
//  ydk-track
//
//  Created by yryz on 2019/6/20.
//

#import <ydk-core/YdkCore.h>

@interface YdkTracker : NSObject <YdkModule>

+ (void)event:(NSString *)eventId attributes:(NSDictionary *)attributes;

+ (void)startTrack:(NSString *)eventName;

+ (void)endTrack:(NSString *)eventName eventData:(NSDictionary *)eventData;

+ (void)identify:(NSString *)userId eventData:(NSDictionary *)eventData;

@end
