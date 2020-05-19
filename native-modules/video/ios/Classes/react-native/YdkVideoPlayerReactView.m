//
//  YdkVideoPlayerReactView.m
//  ydk-video
//
//  Created by yryz on 2019/7/15.
//

#import "YdkVideoPlayerReactView.h"

#import <React/UIView+React.h>
#import <React/RCTLog.h>

@interface YdkVideoPlayerReactView () <YdkVideoPlayerDelegate>

@end

@implementation YdkVideoPlayerReactView

RCT_NOT_IMPLEMENTED(- (instancetype)initWithFrame:(CGRect)frame)
RCT_NOT_IMPLEMENTED(- (instancetype)initWithCoder:(NSCoder *)aDecoder)

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher {
    RCTAssertParam(eventDispatcher);
    if ((self = [super initWithFrame:CGRectZero])) {
        _videoView = [[YdkVideoPlayerView alloc] initWithFrame:CGRectZero];
        _videoView.delegate = self;
        [self addSubview:_videoView];
    }
    return self;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    _videoView.frame = self.bounds;
}

- (void)invokeDirectEventData:(NSDictionary *)body eventBlock:(RCTDirectEventBlock)block {
    NSMutableDictionary *dict = [NSMutableDictionary dictionaryWithDictionary:body];
    [dict setObject:self.reactTag forKey:@"target"];
    if (block) {
        block(dict);
    }
}

// MARK: - YdkVideoPlayerDelegate
- (void)videoPlayerStatusChanged:(YdkVideoPlayerStatus)status {
    RCTDirectEventBlock block;
    switch (status) {
        case YdkVideoPlayerStatusReadyToPlay:
            block = self.onReadyToPlay;
            break;
        case YdkVideoPlayerStatusLoading:
            block = self.onVideoLoad;
            break;
        case YdkVideoPlayerStatusLoaded:
            block = self.onVideoLoadEnd;
            break;
        case YdkVideoPlayerStatusStop:
            block = self.onPlayEnd;
            break;
        default:
            break;
    }
    if (block) [self invokeDirectEventData:nil eventBlock:block];
}

- (void)videoPlayerUpdateProgress:(Float64)progress duration:(Float64)duration playableDuration:(Float64)playableDuration {
    NSDictionary *body = @{@"progress": [NSNumber numberWithDouble:progress],
                           @"duration": [NSNumber numberWithDouble:duration],
                           @"playableDuration": [NSNumber numberWithDouble:playableDuration]};
    [self invokeDirectEventData:body eventBlock:self.onVideoProgress];
}

- (void)videoPlayerFailedWithError:(NSError *)error {
    NSDictionary *body = @{@"domain": error.domain,
                           @"code": @(error.code),
                           @"description": error.localizedDescription ? : @"播放失败"};
    [self invokeDirectEventData:body eventBlock:self.onPlayError];
}

@end
