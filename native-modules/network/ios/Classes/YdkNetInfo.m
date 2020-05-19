//
//  YdkNetInfo.m
//  ydk-network
//
//  Created by yryz on 2019/6/25.
//

#import "YdkNetInfo.h"

#import <objc/runtime.h>
#import <AFNetworking/AFNetworkReachabilityManager.h>

@interface YdkNetInfo ()

@property (nonatomic) NSHashTable<YdkNetStatusChangedBlock> *observers;

@property (nonatomic) NSLock *observersLock;

@end

@implementation YdkNetInfo

__attribute__((constructor)) void before(){
    [YdkNetInfo sharedInstance];
}

+ (instancetype)sharedInstance {
    static YdkNetInfo *sharedInstance = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[self alloc] init];
    });
    return sharedInstance;
}

- (instancetype)init {
    if (self = [super init]) {
        _observers = [NSHashTable weakObjectsHashTable];
        _observersLock = [NSLock new];
        [self _setupNetworkStatus];
    }
    return self;
}

///获取网络状态
- (void)_setupNetworkStatus {
    AFNetworkReachabilityManager *manager = [AFNetworkReachabilityManager sharedManager];
    [self _setNetworkStatus:manager.networkReachabilityStatus];

    __weak __typeof(self)weakSelf = self;
    [manager setReachabilityStatusChangeBlock:^(AFNetworkReachabilityStatus status) {
        __strong __typeof(weakSelf) strongSelf = weakSelf;
        dispatch_async(dispatch_get_main_queue(), ^{
            [strongSelf handleReachabilityChanged:status];
        });
    }];
    [manager startMonitoring];
}

- (void)handleReachabilityChanged:(AFNetworkReachabilityStatus)status {
    YdkNetStatus netStatus = [self _setNetworkStatus:status];
    [self.observersLock lock];
    for (YdkNetStatusChangedBlock callback in self.observers) {
        callback(netStatus);
    }
    [self.observersLock unlock];
}

- (YdkNetStatus)_setNetworkStatus:(AFNetworkReachabilityStatus)status {
    switch (status) {
        case AFNetworkReachabilityStatusUnknown:
            _netStatus = YdkNetStatusUnknown;
            break;
        case AFNetworkReachabilityStatusNotReachable:
            _netStatus = YdkNetStatusNone;
            break;
        case AFNetworkReachabilityStatusReachableViaWWAN:
            _netStatus = YdkNetStatusWWAN;
            break;
        case AFNetworkReachabilityStatusReachableViaWiFi:
            _netStatus = YdkNetStatusWiFi;
            break;
    }
    return _netStatus;
}

static const NSString * YdkNetInfoNetStatusChangeCallbackKey = @"YdkNetInfoNetStatusChangeCallbackKey";
- (void)addObserver:(id)observer changeCallback:(YdkNetStatusChangedBlock)callback {
    [_observersLock lock];
    objc_setAssociatedObject(observer, &YdkNetInfoNetStatusChangeCallbackKey, callback, OBJC_ASSOCIATION_COPY_NONATOMIC);
    [_observers addObject:objc_getAssociatedObject(observer, &YdkNetInfoNetStatusChangeCallbackKey)];
    [_observersLock unlock];
}

+ (void)addObserver:(id)observer changeCallback:(YdkNetStatusChangedBlock)callback; {
    [[self sharedInstance] addObserver:observer changeCallback:callback];
}

@end
