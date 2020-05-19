//
//  YdkAudioPlayerModule.m
//  ydk-audio
//
//  Created by yryz on 2019/7/10.
//

#import "YdkAudioPlayerModule.h"
#import "YdkAudioPlayerManager.h"

static NSString *kOnReadyToPlay = @"OnReadyToPlay";
static NSString *kOnAudioLoad = @"OnAudioLoad";
static NSString *kOnAudioLoadEnd = @"OnAudioLoadEnd";
static NSString *kOnAudioProgress = @"OnAudioProgress";
static NSString *kOnAudioError = @"OnAudioError";
static NSString *kOnAudioEnd = @"OnAudioEnd";
static NSString *kOnPlaybackStalled = @"OnPlaybackStalled";

@interface YdkAudioPlayerModule () <YdkAudioPlayerDelegate>

@end

@implementation YdkAudioPlayerModule
{
    NSNumber *_tagId;
    BOOL _hasListeners;
}

RCT_EXPORT_MODULE(YdkAudioPlayerModule)

- (instancetype)init {
    if (self = [super init]) {
        
    }
    return self;
}

- (NSDictionary *)constantsToExport {
    return @{kOnAudioLoad: kOnAudioLoad,
             kOnAudioLoadEnd: kOnAudioLoadEnd,
             kOnAudioProgress: kOnAudioProgress,
             kOnAudioError: kOnAudioError,
             kOnAudioEnd: kOnAudioEnd,
             kOnPlaybackStalled: kOnPlaybackStalled,
             kOnReadyToPlay: kOnReadyToPlay
             };
}

- (NSArray<NSString *> *)supportedEvents {
    return @[kOnReadyToPlay, kOnAudioLoad, kOnAudioLoadEnd, kOnAudioProgress, kOnAudioError, kOnAudioEnd, kOnPlaybackStalled];
}

+ (BOOL)requiresMainQueueSetup {
    return NO;
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

- (void)startObserving {
    _hasListeners = YES;
}

- (void)stopObserving {
    _hasListeners = NO;
}

- (void)sendEventWithName:(NSString *)name body:(NSDictionary *)body {
    if (_hasListeners) {
        NSMutableDictionary *bodyF = [NSMutableDictionary dictionary];
        if (body) {
            [bodyF addEntriesFromDictionary:body];
        }
        bodyF[@"tagId"] = [_tagId copy];
        [super sendEventWithName:name body:bodyF];
    }
}

RCT_EXPORT_METHOD(play:(nonnull NSDictionary *)source) {
    NSNumber *tagId = [source objectForKey:@"tagId"];
    NSString *url = [source objectForKey:@"url"];
    NSURL *URL;
    if ([url hasPrefix:@"http"] || [url hasPrefix:@"file"]) {
        URL = [NSURL URLWithString:url];
    } else {
        URL = [NSURL fileURLWithPath:url];
    }
    
    _tagId = [tagId copy];
    [YdkAudioPlayerManager playWithURL:URL tagId:tagId delegate:self];
}

RCT_EXPORT_METHOD(pause:(nonnull NSDictionary *)source) {
    [YdkAudioPlayerManager.sharedInstance.player pause];
}

RCT_EXPORT_METHOD(resume:(nonnull NSDictionary *)source) {
    [YdkAudioPlayerManager.sharedInstance.player play];
}

RCT_EXPORT_METHOD(stop:(nonnull NSDictionary *)source) {
    [YdkAudioPlayerManager.sharedInstance.player stop];
    [self sendEventWithName:kOnAudioEnd body:@{@"tagId" : _tagId}];
}

RCT_EXPORT_METHOD(seekToTime:(nonnull NSDictionary *)source) {
    id time = [source objectForKey:@"time"];
    NSTimeInterval t = 0;
    if (time && [time isKindOfClass:[NSNumber class]]) {
        t = [time doubleValue];
    }
    [YdkAudioPlayerManager.sharedInstance.player seekToTime:t];
}

// MARK: - YdkAudioPlayerDelegate
- (void)audioPlayerStatusChanged:(YdkAudioPlayerStatus)status {
    switch (status) {
        case YdkAudioPlayerStatusReadyToPlay:
            [self sendEventWithName:kOnReadyToPlay body:nil];
            break;
        case YdkAudioPlayerStatusPause:
            [self sendEventWithName:kOnPlaybackStalled body:nil];
            break;
        case YdkAudioPlayerStatusLoading:
            [self sendEventWithName:kOnAudioLoad body:nil];
            break;
        case YdkAudioPlayerStatusStop:
            [self sendEventWithName:kOnAudioEnd body:nil];
            break;
        case YdkAudioPlayerStatusLoaded:
            [self sendEventWithName:kOnAudioLoadEnd body:nil];
            break;
        default:
            break;
    }
}

- (void)audioPlayerUpdateProgress:(Float64)progress duration:(Float64)duration playableDuration:(Float64)playableDuration {
    NSDictionary *body = @{@"progress": [NSNumber numberWithDouble:progress],
                           @"duration": [NSNumber numberWithDouble:duration],
                           @"playableDuration": [NSNumber numberWithDouble:playableDuration]};
    [self sendEventWithName:kOnAudioProgress body:body];
}

- (void)audioPlayerFailedWithError:(NSError *)error {
    NSDictionary *body = @{@"domain": error.domain,
                           @"code": @(error.code),
                           @"description": error.localizedDescription ? : @"播放失败"};
    [self sendEventWithName:kOnAudioError body:body];
}

@end
