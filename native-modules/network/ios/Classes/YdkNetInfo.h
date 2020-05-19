//
//  YdkNetInfo.h
//  ydk-network
//
//  Created by yryz on 2019/6/25.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, YdkNetStatus) {
    YdkNetStatusUnknown     = -1,
    YdkNetStatusNone        = 0,
    YdkNetStatusWWAN        = 1,
    YdkNetStatusWiFi        = 2,
};

typedef void (^YdkNetStatusChangedBlock)(YdkNetStatus netStatus);

@interface YdkNetInfo : NSObject

@property (readonly, nonatomic, assign) YdkNetStatus netStatus;

+ (instancetype)sharedInstance;

+ (void)addObserver:(id)observer changeCallback:(YdkNetStatusChangedBlock)callback;
//+ (void)removeObserver:(NSObject *)observer;

@end

