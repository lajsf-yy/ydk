//
//  YdkVideoPlayerProtocol.h
//  ydk-video
//
//  Created by yryz on 2019/7/15.
//

#import <Foundation/Foundation.h>

FOUNDATION_EXPORT NSErrorDomain const YdkVideoPlayerErrorDomain;

NS_ERROR_ENUM(YdkVideoPlayerErrorDomain)
{
    YdkVideoPlayerErrorInvalidFileURL                   = -1000,
    YdkVideoPlayerErrorPlayerItemFailedToPlayToEndTime  = -1001,
    YdkVideoPlayerErrorPlayerItemStatusFailed           = -1002,
};

typedef NS_ENUM(NSInteger, YdkVideoPlayerStatus) {
    YdkVideoPlayerStatusNoraml = 0,
    YdkVideoPlayerStatusReadyToPlay,
    YdkVideoPlayerStatusPlay,
    YdkVideoPlayerStatusPause,
    YdkVideoPlayerStatusLoading,
    YdkVideoPlayerStatusStop,
    YdkVideoPlayerStatusLoaded,
};

@class YdkVideoPlayer;
@protocol YdkVideoPlayerDelegate <NSObject>

@optional
// 1. 状态切换
- (void)videoPlayerStatusChanged:(YdkVideoPlayerStatus)status;

// 2. 进度
- (void)videoPlayerUpdateProgress:(Float64)progress duration:(Float64)duration playableDuration:(Float64)playableDuration;

// 3. 异常
- (void)videoPlayerFailedWithError:(NSError *)error;

@end

@protocol YdkVideoPlayerControl <NSObject>

@property (readonly, nonatomic, assign, getter=isPlaying) BOOL playing;
@property (nonatomic, assign) BOOL autoPlay;    // default is NO.
@property (nonatomic) float volume;

@property (nonatomic, assign) NSTimeInterval currentPlaybackTime;
@property (readonly, nonatomic, assign)  NSTimeInterval duration;
@property (readonly, nonatomic, assign)  NSTimeInterval playableDuration;
@property (readonly, nonatomic, assign)  NSInteger bufferingProgress;
@property (nonatomic, readonly)  BOOL isPreparedToPlay;

- (void)play;
- (void)pause;
- (void)seekToTime:(NSTimeInterval)time;
- (void)stop;

@end
