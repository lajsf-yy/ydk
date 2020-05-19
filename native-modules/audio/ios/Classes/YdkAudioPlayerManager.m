//
//  YdkAudioPlayerManager.m
//  ydk-audio
//
//  Created by yryz on 2019/7/10.
//

#import "YdkAudioPlayerManager.h"
#import "YdkAudioPlayer.h"

@interface YdkAudioPlayerManager () <YdkAudioPlayerDelegate>

@property (nonatomic, strong) YdkAudioPlayer *audioPlayer;

@property (nonatomic, weak) id<YdkAudioPlayerDelegate> delegate;

@end

@implementation YdkAudioPlayerManager
{
    NSNumber *_currentTagId;
}

+ (instancetype)sharedInstance {
    static YdkAudioPlayerManager *instance;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        instance = [[YdkAudioPlayerManager alloc] init];
    });
    return instance;
}

- (instancetype)init {
    if (self = [super init]) {
        _audioPlayer = [[YdkAudioPlayer alloc] init];
        _audioPlayer.delegate = self;
    }
    return self;
}

- (id<YdkAudioPlayerControl>)player {
    return _audioPlayer;
}

+ (void)playWithURL:(NSURL *)url tagId:(NSNumber *)tagId delegate:(id<YdkAudioPlayerDelegate>)delegate {
    [[self sharedInstance] playWithURL:url tagId:tagId delegate:delegate];
}

- (void)playWithURL:(NSURL *)url tagId:(NSNumber *)tagId delegate:(id<YdkAudioPlayerDelegate>)delegate {
    if (_currentTagId && ![tagId isEqualToNumber:_currentTagId]) {
        [_audioPlayer pause];
        [_delegate audioPlayerStatusChanged:YdkAudioPlayerStatusStop];
    }
    _currentTagId = [tagId copy];
    _delegate = delegate;
    
    [_audioPlayer prepareWithURL:url autoPlay:YES];
}

// MARK: - YdkAudioPlayerDelegate
- (void)audioPlayerStatusChanged:(YdkAudioPlayerStatus)status {
    if ([_delegate respondsToSelector:@selector(audioPlayerStatusChanged:)]) {
        [_delegate audioPlayerStatusChanged:status];
    }
}

- (void)audioPlayerUpdateProgress:(Float64)progress duration:(Float64)duration playableDuration:(Float64)playableDuration {
    if ([_delegate respondsToSelector:@selector(audioPlayerUpdateProgress:duration:playableDuration:)]) {
        [_delegate audioPlayerUpdateProgress:progress duration:duration playableDuration:playableDuration];
    }
}

- (void)audioPlayerFailedWithError:(NSError *)error {
    if ([_delegate respondsToSelector:@selector(audioPlayerFailedWithError:)]) {
        [_delegate audioPlayerFailedWithError:error];
    }
}

@end
