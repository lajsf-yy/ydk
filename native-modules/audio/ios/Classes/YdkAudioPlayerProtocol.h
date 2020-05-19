//
//  YdkAudioPlayerProtocol.h
//  ydk-audio
//
//  Created by yryz on 2019/7/10.
//

#import <Foundation/Foundation.h>

FOUNDATION_EXPORT NSErrorDomain const YdkAudioPlayerErrorDomain;

NS_ERROR_ENUM(YdkAudioPlayerErrorDomain)
{
    YdkAudioPlayerErrorInvalidFileURL                   = -1000,
    YdkAudioPlayerErrorPlayerItemFailedToPlayToEndTime  = -1001,
    YdkAudioPlayerErrorPlayerItemStatusFailed           = -1002,
};

typedef NS_ENUM(NSInteger, YdkAudioPlayerStatus) {
    YdkAudioPlayerStatusNoraml = 0,
    YdkAudioPlayerStatusReadyToPlay,
    YdkAudioPlayerStatusPlay,
    YdkAudioPlayerStatusPause,
    YdkAudioPlayerStatusLoading,
    YdkAudioPlayerStatusStop,
    YdkAudioPlayerStatusLoaded
};

@class YdkAudioPlayer;
@protocol YdkAudioPlayerDelegate <NSObject>

@optional
// 1. 状态切换
- (void)audioPlayerStatusChanged:(YdkAudioPlayerStatus)status;

// 2. 进度
- (void)audioPlayerUpdateProgress:(Float64)progress duration:(Float64)duration playableDuration:(Float64)playableDuration;

// 3. 异常
- (void)audioPlayerFailedWithError:(NSError *)error;

@end

@protocol YdkAudioPlayerControl <NSObject>

@property (readonly, nonatomic, assign, getter=isPlaying) BOOL playing;

- (void)play;
- (void)pause;
- (void)stop;
- (void)seekToTime:(NSTimeInterval)time;

@end
