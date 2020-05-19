//
//  YdkAudioPlayer.m
//  ydk-audio
//
//  Created by yryz on 2019/7/10.
//

#import "YdkAudioPlayer.h"
#import "AVPlayerItem+MCCacheSupport.h"

#import <ydk-toolkit/YdkToolkit.h>
#import <AVFoundation/AVFoundation.h>

NSErrorDomain const YdkAudioPlayerErrorDomain = @"YdkAudioPlayerErrorDomain";

static NSString *const statusKeyPath = @"status";
static NSString *const playbackLikelyToKeepUpKeyPath = @"playbackLikelyToKeepUp";
static NSString *const playbackBufferEmptyKeyPath = @"playbackBufferEmpty";
static NSString *const playbackBufferFullKeyPath = @"playbackBufferFull";
static NSString *const playbackRate = @"rate";

@interface YdkAudioPlayer ()

@end

@implementation YdkAudioPlayer
{
    
    NSURL *_audioURL;
    BOOL _autoPlay;
    
    // AVPlayer
    AVPlayer *_player;
    AVPlayerItem *_playerItem;
    
    //
    BOOL _playerItemObserversSet;           // AVPlayerItem的KVO监听
    BOOL _playbackRateObserverRegistered;   // 注册rate的KVO
    BOOL _playerItemListeners;
    BOOL _playbackStalled;  // 播放是否停止
    BOOL _playerBufferEmpty;    // 缓冲数据是否为空
    
    // Sending audioProgress events
    Float64 _progressUpdateInterval;
    id _timeObserver;
    
    Float64 _currentTime, _nowPlayingTime;
    Float64 _duration;
    
    BOOL _isPlay, _nowPlaying;
    
    UIBackgroundTaskIdentifier currentTaskId;
    YdkAudioPlayerStatus _playerStatus;
}

- (void)prepareWithURL:(NSURL *)url autoPlay:(BOOL)autoPlay {
    if (![_audioURL isEqual:url]) {
        _nowPlayingTime = 0;
        if (_player) {
            [_player pause];
            if (_playbackRateObserverRegistered) {
                [_player removeObserver:self forKeyPath:playbackRate context:nil];
                _playbackRateObserverRegistered = NO;
            }
            [[NSNotificationCenter defaultCenter] removeObserver:self];
        }

        _audioURL = [url copy];
        _autoPlay = _isPlay = autoPlay;
        _playerStatus = YdkAudioPlayerStatusNoraml;
        _playbackRateObserverRegistered = NO;
        _playbackStalled = NO;
        _playerBufferEmpty = YES;
        _progressUpdateInterval = 250;
        currentTaskId = UIBackgroundTaskInvalid;
        
        [self prepareAudioToPlay];
        
        _nowPlaying = NO;
    } else {
        _autoPlay = _isPlay = autoPlay;
        if (autoPlay) {
            [self play];
            [self sendAudioPlayerStatusChanged:YdkAudioPlayerStatusReadyToPlay];
        }
    }
}

// MARK: - Setter
- (BOOL)isPlaying {
    return _isPlay;
}

- (void)setVolum:(double)volum {
    _player.volume = volum;
}

- (void)prepareAudioToPlay {
    [self playerClean];
    
    _playerItem = [self playerItemForURL:_audioURL];
    if (!_playerItem) {
        [_player pause];
        [_player replaceCurrentItemWithPlayerItem:nil];
        if (_playbackRateObserverRegistered) {
            [_player removeObserver:self forKeyPath:playbackRate context:nil];
            _playbackRateObserverRegistered = NO;
        }
        _player = nil;
        NSError *error = [NSError errorWithDomain:YdkAudioPlayerErrorDomain code:YdkAudioPlayerErrorInvalidFileURL userInfo:@{NSLocalizedDescriptionKey : @"无效的uri"}];
        [self sendAudioPlayerFailedWithError:error];
        return;
    }
    
    [_player pause];
    
    // player
    if (_playbackRateObserverRegistered) {
        [_player removeObserver:self forKeyPath:playbackRate context:nil];
        _playbackRateObserverRegistered = NO;
    }
    
    _player = [AVPlayer playerWithPlayerItem:_playerItem];
    _player.actionAtItemEnd = AVPlayerActionAtItemEndNone;
    [_player addObserver:self forKeyPath:playbackRate options:NSKeyValueObservingOptionNew context:nil];
    if (@available(iOS 10.0, *)) {
        [_player setAutomaticallyWaitsToMinimizeStalling:NO];
    }
    _playbackRateObserverRegistered = YES;
    
    [self addPlayerTimeObserver];
    
    // init player and playerItem
    [self addPlayerItemObservers];
}

- (AVPlayerItem *)playerItemForURL:(NSURL *)url {
    if (!url) return nil;
    
    bool isHTTP = [url.scheme hasPrefix:@"http"] || [url.scheme hasPrefix:@"file"];
    if (isHTTP) {
        NSArray *cookies = [[NSHTTPCookieStorage sharedHTTPCookieStorage] cookies];
        NSError *error;
        AVPlayerItem *playerItem = [AVPlayerItem playerItemWithAsset:
                                    [AVURLAsset URLAssetWithURL:url options:@{  AVURLAssetHTTPCookiesKey : cookies,
                                                                                AVURLAssetPreferPreciseDurationAndTimingKey : @YES
                                                                                }]];
//         AVPlayerItem *playerItem = [AVPlayerItem mc_playerItemWithRemoteURL:url options:nil error:&error];
        if (error) {
            // NSLog(@"缓存失败 ...");
        }
        return playerItem;
    } else {
        AVURLAsset *asset = [AVURLAsset URLAssetWithURL:url options:nil];
        return [AVPlayerItem playerItemWithAsset:asset];
    }
}


// MARK: - Player item observers
- (void)addPlayerTimeObserver {
    const Float64 progressUpdateIntervalMS = _progressUpdateInterval / 1000;
    __weak typeof(self) weakSelf = self;
    _timeObserver = [_player addPeriodicTimeObserverForInterval:CMTimeMakeWithSeconds(progressUpdateIntervalMS, NSEC_PER_SEC)
                                                          queue:NULL
                                                     usingBlock:^(CMTime time) { [weakSelf sendProgressUpdate:time]; } ];
}

- (void)sendProgressUpdate:(CMTime)time {
    
    AVPlayerItem *audio = [_player currentItem];
    if (audio == nil || audio.status != AVPlayerItemStatusReadyToPlay) {
        return;
    }
    
    CMTime playerDuration = [self playerItemDuration];
    if (CMTIME_IS_INVALID(playerDuration)) {
        return;
    }
    
    _duration = CMTimeGetSeconds(_player.currentItem.duration);
    _currentTime = CMTimeGetSeconds(_player.currentItem.currentTime);
    _currentTime = MIN(_currentTime, _duration);
    if (_currentTime >= 0 && _isPlay) {
        Float64 playable = [_player.currentItem tk_calculatePlayableDuration];
        if (playable > 0) {
            if (!_nowPlaying && _nowPlayingTime > 0) {
                _nowPlaying = !_nowPlaying;
                CMTime cmTime = CMTimeMakeWithSeconds(_nowPlayingTime, _playerItem.currentTime.timescale);
                if (CMTIME_IS_VALID(cmTime)) [_player seekToTime:cmTime toleranceBefore:kCMTimeZero toleranceAfter:kCMTimeZero];
            }
        }
        if (_player.rate == 1.0 &&  _playerItem.isPlaybackLikelyToKeepUp && _playerStatus != YdkAudioPlayerStatusPlay) {
            [self sendAudioPlayerStatusChanged:YdkAudioPlayerStatusPlay];
        }
        if ([_delegate respondsToSelector:@selector(audioPlayerUpdateProgress:duration:playableDuration:)]) {
            [_delegate audioPlayerUpdateProgress:_currentTime duration:_duration playableDuration:playable];
        }
    }
}

- (CMTime)playerItemDuration {
    AVPlayerItem *playerItem = [_player currentItem];
    if (playerItem.status == AVPlayerItemStatusReadyToPlay) {
        return([playerItem duration]);
    }
    return(kCMTimeInvalid);
}

- (void)removePlayerTimeObserver {
    if (_timeObserver) {
        [_player removeTimeObserver:_timeObserver];
        _timeObserver = nil;
    }
}

- (void)addPlayerItemObservers {
    [_playerItem addObserver:self forKeyPath:statusKeyPath options:NSKeyValueObservingOptionNew context:nil];
    [_playerItem addObserver:self forKeyPath:playbackBufferEmptyKeyPath options:NSKeyValueObservingOptionNew context:nil];
    [_playerItem addObserver:self forKeyPath:playbackLikelyToKeepUpKeyPath options:NSKeyValueObservingOptionNew context:nil];
    [_playerItem addObserver:self forKeyPath:playbackBufferFullKeyPath options:NSKeyValueObservingOptionNew context:nil];
    _playerItemObserversSet = YES;
}

- (void)removePlayerItemObservers {
    if (_playerItemObserversSet) {
        [_playerItem removeObserver:self forKeyPath:statusKeyPath];
        [_playerItem removeObserver:self forKeyPath:playbackBufferEmptyKeyPath];
        [_playerItem removeObserver:self forKeyPath:playbackLikelyToKeepUpKeyPath];
        [_playerItem removeObserver:self forKeyPath:playbackBufferFullKeyPath];
        _playerItemObserversSet = NO;
    }
}

- (void)addPlayerItemListeners {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handlePlayerItemDidReachEnd:)
                                                 name:AVPlayerItemDidPlayToEndTimeNotification
                                               object:[_player currentItem]];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handlePlaybackStalled:)
                                                 name:AVPlayerItemPlaybackStalledNotification
                                               object:[_player currentItem]];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(handlePlayFailed:)
                                                 name:AVPlayerItemFailedToPlayToEndTimeNotification
                                               object:[_player currentItem]];
    [[NSNotificationCenter defaultCenter] addObserver: self
                                             selector: @selector(handleInterruption:)
                                                 name: AVAudioSessionInterruptionNotification
     //                                               object: [AVAudioSession sharedInstance]];
                                               object: nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(routeChange:)
                                                 name:AVAudioSessionRouteChangeNotification
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(applicationWillResignActive:)
                                                 name:UIApplicationWillResignActiveNotification
                                               object:nil];
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(applicationWillEnterForeground:)
                                                 name:UIApplicationWillEnterForegroundNotification
                                               object:nil];
    _playerItemListeners = YES;
}

- (void)removePlayerItemListeners {
    if (_playerItemListeners) {
        [[NSNotificationCenter defaultCenter] removeObserver:self name:AVPlayerItemDidPlayToEndTimeNotification object:[_player currentItem]];
        [[NSNotificationCenter defaultCenter] removeObserver:self name:AVPlayerItemPlaybackStalledNotification object:[_player currentItem]];
        [[NSNotificationCenter defaultCenter] removeObserver:self name:AVPlayerItemFailedToPlayToEndTimeNotification object:[_player currentItem]];
        [[NSNotificationCenter defaultCenter] removeObserver:self name:AVAudioSessionInterruptionNotification object:nil];
        [[NSNotificationCenter defaultCenter] removeObserver:self name:AVAudioSessionRouteChangeNotification object:nil];
        [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillResignActiveNotification object:nil];
        [[NSNotificationCenter defaultCenter] removeObserver:self name:UIApplicationWillEnterForegroundNotification object:nil];
        _playerItemListeners = NO;
    }
}

- (void)handlePlaybackStalled:(NSNotification *)notification {
    //    fprintf(stderr, "handlePlaybackStalled");
    //    if(_onPlaybackStalled) _onPlaybackStalled(@{YdkAudioPlayerEventKeyTagId : _currentTagId});
    //    _playbackStalled = YES;
    //    _isPlay = NO;
    //    [self closeAudioSession];
    //    [_player play];
}

- (void)handlePlayerItemDidReachEnd:(NSNotification *)notification {
    if (fabs(CMTimeGetSeconds(_playerItem.duration) - _currentTime) <= 1) {
        [self sendAudioPlayerStatusChanged:YdkAudioPlayerStatusStop];
        _nowPlaying = NO;
        _nowPlayingTime = 0;
        [self pause];
        [self closeAudioSession];
    }
}

- (void)handlePlayFailed:(NSNotification *)notification {
    // AVPlayerItemFailedToPlayToEndTimeErrorKey
    NSError *error = [NSError errorWithDomain: _playerItem.error.domain ? : YdkAudioPlayerErrorDomain code:_playerItem.error.code ? : YdkAudioPlayerErrorPlayerItemFailedToPlayToEndTime userInfo:@{NSLocalizedDescriptionKey : _playerItem.error.localizedDescription ? : @"播放失败"}];
    [self sendAudioPlayerFailedWithError:error];
}

- (void)openAudioSession {
    /**
     * 后台播放视频
     * 在Info.plist文件中添加：UIBackgroundModes-audio
     */
    NSError *error = nil;
    AVAudioSession *session = [AVAudioSession sharedInstance];
    BOOL success = [session setCategory:AVAudioSessionCategoryPlayback error:&error];
    if (!success || error) {
        NSLog(@"openAudioSession error 1%@", error);
        [session setCategory:AVAudioSessionCategoryPlayback error:&error];
    }
    success = [session setActive:YES error:&error];
    if (!success || error) {
        NSLog(@"openAudioSession error 2%@", error);
        [session setActive:YES error:&error];
    }
}

- (void)closeAudioSession {
    AVAudioSession *session = [AVAudioSession sharedInstance];
    [session setActive:NO error:NULL];
}

- (void)routeChange:(NSNotification *)noti {
    NSDictionary *dic = noti.userInfo;
    int changeReason= [dic[AVAudioSessionRouteChangeReasonKey] intValue];
    // 旧输出不可用
    if (changeReason == AVAudioSessionRouteChangeReasonOldDeviceUnavailable) {
        AVAudioSessionRouteDescription *routeDescription = dic[AVAudioSessionRouteChangePreviousRouteKey];
        AVAudioSessionPortDescription *portDescription = [routeDescription.outputs firstObject];
        // 原设备为耳机则暂停
        if ([portDescription.portType isEqualToString:@"Headphones"]) {
            //            UInt32 audioRouteOverride = kAudioSessionOverrideAudioRoute_Speaker;
            //            AVAudioSession * session = [AVAudioSession sharedInstance];
            //            [session setPreferredIOBufferDuration:audioRouteOverride error:nil];
            if (_isPlay) {
                [self pause];
                [self playbackStalled];
            }
        }
    }
}

- (void)handleInterruption:(NSNotification *)noti {
    if (noti.object == self) return;
    
    NSDictionary *userInfo = [noti userInfo];
    AVAudioSessionInterruptionType type = [[userInfo objectForKey:AVAudioSessionInterruptionTypeKey] unsignedIntegerValue];
    switch (type) {
        case AVAudioSessionInterruptionTypeBegan: {
            if (_isPlay) [self pause];
            [self playbackStalled];
        }
            break;
        case AVAudioSessionInterruptionTypeEnded: {
            
        }
            break;
    }
}

- (void)observeValueForKeyPath:(NSString *)keyPath ofObject:(id)object change:(NSDictionary<NSKeyValueChangeKey,id> *)change context:(void *)context {
    if (object == _playerItem) {
        // NSLog(@"keyPath: %@", keyPath);
        // 1. 媒体状态
        if ([keyPath isEqualToString:statusKeyPath]) {
            if (_playerItem.status == AVPlayerItemStatusReadyToPlay) {
                float duration = CMTimeGetSeconds(_playerItem.asset.duration);
                if (isnan(duration)) {
                    duration = 0.0;
                }
                _duration = duration;
                _currentTime = CMTimeGetSeconds(_playerItem.currentTime);
                
                if (_autoPlay && _isPlay) {
                    [self play];
                }
                
                [self addPlayerItemListeners];
                [self sendAudioPlayerStatusChanged:YdkAudioPlayerStatusReadyToPlay];
            } else if(_playerItem.status == AVPlayerItemStatusFailed) {
                NSError *error = [NSError errorWithDomain: _playerItem.error.domain ? : YdkAudioPlayerErrorDomain code:_playerItem.error.code ? : YdkAudioPlayerErrorPlayerItemStatusFailed userInfo:@{NSLocalizedDescriptionKey : _playerItem.error.localizedDescription ? : @"播放失败"}];
                [self sendAudioPlayerFailedWithError:error];
            }
        } else if ([keyPath isEqualToString:playbackBufferEmptyKeyPath]) {
            _playerBufferEmpty = YES;
            if (_isPlay && self.loadState) [self sendAudioPlayerStatusChanged:self.loadState];
        } else if ([keyPath isEqualToString:playbackLikelyToKeepUpKeyPath]) {
            _playerBufferEmpty = NO;
            if (_isPlay) {
                [_player play];
                [_player setRate:1.0];
                if (self.loadState) [self sendAudioPlayerStatusChanged:self.loadState];
            }
        } else if ([keyPath isEqualToString:playbackBufferFullKeyPath]) {
            _playerBufferEmpty = NO;
            if (_isPlay) {
                [_player play];
                [_player setRate:1.0];
                if (self.loadState)  [self sendAudioPlayerStatusChanged:self.loadState];
            }
        }
    } else if (object == _player) {
        if ([keyPath isEqualToString:playbackRate]) {
            if (_playbackStalled && _player.rate > 0) {
                _playbackStalled = NO;
            }
            if (_isPlay && self.loadState) [self sendAudioPlayerStatusChanged:self.loadState];
        }
    } else {
        [super observeValueForKeyPath:keyPath ofObject:object change:change context:context];
    }
}

- (void)applicationWillResignActive:(UIApplication *)application {
    if (_isPlay) {
        AVAudioSession *session = [AVAudioSession sharedInstance];
        UIDevice *device = [UIDevice currentDevice];
        if ([device respondsToSelector:@selector(isMultitaskingSupported)]) {
            if (device.multitaskingSupported) {
                if (device.multitaskingSupported) {
                    if (currentTaskId == UIBackgroundTaskInvalid) {
                        currentTaskId = [[UIApplication sharedApplication] beginBackgroundTaskWithExpirationHandler:NULL];
                    }
                }
            }
        }
    }
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    if (currentTaskId != UIBackgroundTaskInvalid) {
        [[UIApplication sharedApplication] endBackgroundTask:currentTaskId];
        currentTaskId = UIBackgroundTaskInvalid;
    }
}

// MARK: - Actions
- (void)play {
    _isPlay = YES;
    if (!_player || _player.status != AVPlayerStatusReadyToPlay) {
        return;
    }
    if (_duration > 0 && round(_currentTime) >= round(_duration)) {  // 避免小数位置影响
        _currentTime = 0.0;
        [self seekToTime:_currentTime];
    }
    [self openAudioSession];
    
    [_player play];
    [_player setRate:1.0];
    
    NSDictionary *userInfo = @{AVAudioSessionInterruptionTypeKey : @(AVAudioSessionInterruptionTypeBegan)};
    [[NSNotificationCenter defaultCenter] postNotificationName:AVAudioSessionInterruptionNotification object:self userInfo:userInfo];
}

- (void)pause {
    [_player pause];
    [_player setRate:0.0];
    _isPlay = NO;
}

- (void)stop {
    [self pause];
    //[self seekToTime:0];
    [self closeAudioSession];
}

- (void)seekToTime:(NSTimeInterval)time {
    
    _currentTime = time;
    _nowPlayingTime = time;
    
    if (_playerStatus != YdkAudioPlayerStatusNoraml) {
        CMTime cmTime = CMTimeMakeWithSeconds(time, _playerItem.currentTime.timescale);
        if (CMTIME_IS_VALID(cmTime)) [_player seekToTime:cmTime toleranceBefore:kCMTimeZero toleranceAfter:kCMTimeZero];
    }
}

- (void)playbackStalled {
    [self sendAudioPlayerStatusChanged:YdkAudioPlayerStatusPause];
    _playbackStalled = YES;
    _isPlay = NO;
    [self closeAudioSession];
}

- (void)dealloc {
    [self playerClean];
}

- (void)playerClean {
    [_player pause];
    if (_playbackRateObserverRegistered) {
        [_player removeObserver:self forKeyPath:playbackRate context:nil];
        _playbackRateObserverRegistered = NO;
    }
    _player = nil;
    
    [self removePlayerTimeObserver];
    [self removePlayerItemObservers];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

// MARK: - Utils
- (void)sendAudioPlayerStatusChanged:(YdkAudioPlayerStatus)status {
    if (_playerStatus != status) {
        _playerStatus = status;
        if ([_delegate respondsToSelector:@selector(audioPlayerStatusChanged:)]) {
            [_delegate audioPlayerStatusChanged:status];
        }
    }
}

- (void)sendAudioPlayerFailedWithError:(NSError *)error {
    if ([_delegate respondsToSelector:@selector(audioPlayerFailedWithError:)]) {
        [_delegate audioPlayerFailedWithError:error];
    }
}

- (YdkAudioPlayerStatus)loadState {
    if (_player == nil)
        return YdkAudioPlayerStatusNoraml;
    
    AVPlayerItem *playerItem = [_player currentItem];
    if (playerItem == nil)
        return YdkAudioPlayerStatusNoraml;
    
    if (_player != nil && !isFloatZero(_player.rate)) {
        return YdkAudioPlayerStatusLoaded;
    } else if ([playerItem isPlaybackBufferFull]) {
        return YdkAudioPlayerStatusLoaded;
    } else if ([playerItem isPlaybackLikelyToKeepUp]) {
        return YdkAudioPlayerStatusLoaded;
    } else if ([playerItem isPlaybackBufferEmpty]) {
        return YdkAudioPlayerStatusLoading;
    } else {
        return YdkAudioPlayerStatusNoraml;
    }
}

inline static bool isFloatZero(float value) {
    return fabsf(value) <= 0.00001f;
}

@end
