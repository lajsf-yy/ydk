//
//  YVideoView.m
//  ydk
//
//  Created by 悠然一指 on 2018/6/4.
//  Copyright © 2018年 悠然一指. All rights reserved.
//

#import "YdkVideoPlayerView.h"

#import <ydk-toolkit/YdkToolkit.h>
#import <AVFoundation/AVFoundation.h>
#import <ijkplayer/IJKMediaFramework.h>

NSErrorDomain const YdkVideoPlayerErrorDomain = @"YdkVideoPlayerErrorDomain";

@interface YdkVideoPlayerView ()

@property (nonatomic, strong) IJKFFMoviePlayerController *player;
@property (nonatomic, strong) CADisplayLink *displayLink;

@end

@implementation YdkVideoPlayerView
{
    BOOL _addPlayerTimeObserver;
    BOOL _readyToPlayBeforePlay;
    NSInteger _rotation;
}
@synthesize autoPlay = _autoPlay;

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
    }
    return self;
}

- (instancetype)initWithCoder:(NSCoder *)coder {
    if (self = [super initWithCoder:coder]) {
        
    }
    return self;
}

// MARK: - Setter Getter
- (BOOL)isPlaying {
    return _player.isPlaying;
}

- (BOOL)isPreparedToPlay {
    return _player.isPreparedToPlay;
}

- (float)volume {
    return _player.playbackVolume;
}

- (void)setVolume:(float)volume {
    [_player setPlaybackVolume:volume];
}

- (NSTimeInterval)currentPlaybackTime {
    return _player.currentPlaybackTime;
}

- (void)setCurrentPlaybackTime:(NSTimeInterval)currentPlaybackTime {
    [_player setCurrentPlaybackTime:currentPlaybackTime];
}

- (NSTimeInterval)duration {
    return _player.duration;
}

- (NSTimeInterval)playableDuration {
    return _player.playableDuration;
}

- (NSInteger)bufferingProgress {
    return _player.bufferingProgress;
}

- (void)setAutoPlay:(BOOL)autoPlay {
    _autoPlay = autoPlay;
}

- (BOOL)autoPlay {
    return _autoPlay;
}

- (void)setSource:(NSDictionary *)source {
    _source = [source copy];
    
    NSString *videoGravity = [source objectForKey:@"videoGravity"];
    IJKMPMovieScalingMode scalingMode = IJKMPMovieScalingModeAspectFit;
    if (videoGravity) {
        if ([videoGravity isEqualToString:@"aspect"]) {
            scalingMode = IJKMPMovieScalingModeAspectFit;
        } else if ([videoGravity isEqualToString:@"aspectFill"]) {
            scalingMode = IJKMPMovieScalingModeAspectFill;
        } else if ([videoGravity isEqualToString:@"resize"]) {
            scalingMode = IJKMPMovieScalingModeFill;
        }
    }
    NSString *uri = [source objectForKey:@"uri"];
    NSURL *url = [NSURL URLWithString:uri];
    if (!uri || ![uri isKindOfClass:[NSString class]] || !uri.length || !url) {
        NSError *error = [NSError errorWithDomain:YdkVideoPlayerErrorDomain code:YdkVideoPlayerErrorInvalidFileURL userInfo:@{NSLocalizedDescriptionKey : @"无效的uri"}];
        [self sendVideoPlayerFailedWithError:error];
        return;
    }
    [self.player stop];
    [self.player shutdown];
    [self.player.view removeFromSuperview];
    [self removePlayerTimeObserver];
    [self removePlayerNotificationListeners];
    self.player = nil;
    _readyToPlayBeforePlay = NO;
    
#ifdef DEBUG
    [IJKFFMoviePlayerController setLogReport:YES];
    [IJKFFMoviePlayerController setLogLevel:k_IJK_LOG_WARN];
#endif
    
    [IJKFFMoviePlayerController checkIfFFmpegVersionMatch:YES];
    // [IJKFFMoviePlayerController checkIfPlayerVersionMatch:YES major:1 minor:0 micro:0];
    
    IJKFFOptions *options = [IJKFFOptions optionsByDefault];
     _rotation = [self degressFromVideoFileWithURL:url];
    self.player = [[IJKFFMoviePlayerController alloc] initWithContentURL:url withOptions:options];
    self.player.view.autoresizingMask = UIViewAutoresizingFlexibleWidth|UIViewAutoresizingFlexibleHeight;
    // self.player.view.frame = self.bounds;
    // 判断旋转角度
    if (_rotation == 90) {
        self.player.view.frame = CGRectMake(self.player.view.frame.origin.x, self.player.view.frame.origin.y, self.bounds.size.height, self.bounds.size.width);
        self.player.view.transform = CGAffineTransformMakeRotation(M_PI_2);
        self.player.view.frame = CGRectMake(0,0, self.player.view.frame.size.width, self.player.view.frame.size.height);
    } else {
        self.player.view.frame = self.bounds;
    }
    
    self.player.scalingMode = scalingMode;
    self.player.shouldAutoplay = _autoPlay;
    [self.player setPauseInBackground:YES];
    // self.layer.needsDisplayOnBoundsChange = YES;
    
    self.autoresizesSubviews = YES;
    [self addSubview:self.player.view];
    
    [self addPlayerNotificationListeners];
    [self.player prepareToPlay];
    [self addPlayerTimeObserver];
}

// MARK: - Player observers
- (void)addPlayerTimeObserver {
    if (!_addPlayerTimeObserver) {
        _addPlayerTimeObserver = YES;
        _displayLink = [CADisplayLink displayLinkWithTarget:self selector:@selector(sendProgressUpdate)];
        // 1s4次
        if (@available(iOS 11.0, *)) {
            _displayLink.preferredFramesPerSecond = 4;
        } else {
            _displayLink.frameInterval = 15;
        }
        [_displayLink addToRunLoop:[NSRunLoop mainRunLoop] forMode:NSRunLoopCommonModes];
    }
}

- (void)addPlayerNotificationListeners {
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(loadStateDidChange:)
                                                 name:IJKMPMoviePlayerLoadStateDidChangeNotification
                                               object:_player];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(moviePlayBackDidFinish:)
                                                 name:IJKMPMoviePlayerPlaybackDidFinishNotification
                                               object:_player];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(mediaIsPreparedToPlayDidChange:)
                                                 name:IJKMPMediaPlaybackIsPreparedToPlayDidChangeNotification
                                               object:_player];
    
    [[NSNotificationCenter defaultCenter] addObserver:self
                                             selector:@selector(moviePlayBackStateDidChange:)
                                                 name:IJKMPMoviePlayerPlaybackStateDidChangeNotification
                                               object:_player];
}

- (void)removePlayerNotificationListeners {
    [[NSNotificationCenter defaultCenter]removeObserver:self name:IJKMPMoviePlayerLoadStateDidChangeNotification object:_player];
    [[NSNotificationCenter defaultCenter]removeObserver:self name:IJKMPMoviePlayerPlaybackDidFinishNotification object:_player];
    [[NSNotificationCenter defaultCenter]removeObserver:self name:IJKMPMediaPlaybackIsPreparedToPlayDidChangeNotification object:_player];
    [[NSNotificationCenter defaultCenter]removeObserver:self name:IJKMPMoviePlayerPlaybackStateDidChangeNotification object:_player];
}

- (void)loadStateDidChange:(NSNotification*)notification {
    IJKMPMovieLoadState loadState = _player.loadState;
    if ((loadState & IJKMPMovieLoadStatePlaythroughOK) != 0) {
        DLog(@"IJKMPMovieLoadStatePlaythroughOK: %d", (int)loadState);
        [self sendVideoPlayerStatusChanged:YdkVideoPlayerStatusLoaded];
    } else if ((loadState & IJKMPMovieLoadStateStalled) != 0) {
        DLog(@"IJKMPMovieLoadStateStalled: %d", (int)loadState);
        [self sendVideoPlayerStatusChanged:YdkVideoPlayerStatusLoading];
    } else if ((loadState & IJKMPMovieLoadStatePlayable) != 0) {
        DLog(@"IJKMPMovieLoadStatePlayable: %d", (int)loadState);
        [self sendVideoPlayerStatusChanged:YdkVideoPlayerStatusLoaded];
    } else {
        DLog(@"loadStateDidChange: ???: %d\n", (int)loadState);
    }
}

- (void)moviePlayBackDidFinish:(NSNotification*)notification {
    int reason = [[[notification userInfo] valueForKey:IJKMPMoviePlayerPlaybackDidFinishReasonUserInfoKey] intValue];
    switch (reason) {
        case IJKMPMovieFinishReasonPlaybackEnded:
            DLog(@"IJKMPMovieFinishReasonPlaybackEnded: %d", reason);
//            [self sendVideoPlayerStatusChanged:YdkVideoPlayerStatusStop];
//            [self removePlayerTimeObserver];
            break;

        case IJKMPMovieFinishReasonUserExited:
            DLog(@"IJKMPMovieFinishReasonUserExited: %d", reason);
            [self sendVideoPlayerStatusChanged:YdkVideoPlayerStatusStop];
            [self removePlayerTimeObserver];
            break;

        case IJKMPMovieFinishReasonPlaybackError: {
            DLog(@"IJKMPMovieFinishReasonPlaybackError: %d", reason);
            NSError *error = [NSError errorWithDomain:YdkVideoPlayerErrorDomain code:reason userInfo:nil];
            [self sendVideoPlayerFailedWithError:error];
        }
            break;
        default:
            DLog(@"playbackPlayBackDidFinish: ???: %d\n", reason);
            break;
    }
}

- (void)mediaIsPreparedToPlayDidChange:(NSNotification*)notification {
    [self sendVideoPlayerStatusChanged:YdkVideoPlayerStatusReadyToPlay];
    if (_readyToPlayBeforePlay) {
        [_player play];
    }
} 

- (void)moviePlayBackStateDidChange:(NSNotification*)notification {
    switch (_player.playbackState)
    {
        case IJKMPMoviePlaybackStateStopped: {
            DLog(@"IJKMPMoviePlaybackStateStopped");
            [self removePlayerTimeObserver];
            if ([_delegate respondsToSelector:@selector(videoPlayerUpdateProgress:duration:playableDuration:)]) {
                [_delegate videoPlayerUpdateProgress:_player.duration duration:_player.duration playableDuration:_player.playableDuration];
            }
            [self sendVideoPlayerStatusChanged:YdkVideoPlayerStatusStop];
            break;
        }
        case IJKMPMoviePlaybackStatePlaying: {
            DLog(@"IJKMPMoviePlaybackStatePlaying");
            [self sendVideoPlayerStatusChanged:YdkVideoPlayerStatusPlay];
            break;
        }
        case IJKMPMoviePlaybackStatePaused: {
            DLog(@"IJKMPMoviePlaybackStatePaused");
            [self sendVideoPlayerStatusChanged:YdkVideoPlayerStatusPause];
            break;
        }
        case IJKMPMoviePlaybackStateInterrupted: {
            DLog(@"IJKMPMoviePlaybackStateInterrupted");
            [self sendVideoPlayerStatusChanged:YdkVideoPlayerStatusPause];
            [self removePlayerTimeObserver];
            break;
        }
        case IJKMPMoviePlaybackStateSeekingForward:
        case IJKMPMoviePlaybackStateSeekingBackward: {
            DLog(@"IJKMPMoviePlaybackStateSeeking");
            break;
        }
        default: {
            DLog(@"IJKMPMoviePlayBackStateDidChange %d: unknown", (int)_player.playbackState);
            break;
        }
    }
}

- (void)removePlayerTimeObserver {
    if (_addPlayerTimeObserver) {
        _addPlayerTimeObserver = NO;
        if (_displayLink) {
            [_displayLink invalidate];
            _displayLink = nil;
        }
    }
}

- (void)sendProgressUpdate {
    if (_player == nil || _player.playbackState != IJKMPMoviePlaybackStatePlaying) {
        return;
    }
    if ([_delegate respondsToSelector:@selector(videoPlayerUpdateProgress:duration:playableDuration:)]) {
        NSTimeInterval duration = _player.duration;
        NSTimeInterval playableDuration = _player.playableDuration;
        [_delegate videoPlayerUpdateProgress:_player.currentPlaybackTime duration:duration playableDuration:_player.playableDuration];
    }
}

// MARK: - Actions
- (void)play {
    if (_player.isPreparedToPlay) {
        _readyToPlayBeforePlay = NO;
        [_player play];
        [self addPlayerTimeObserver];
    } else {
        _readyToPlayBeforePlay = YES;
    }
}

- (void)pause {
    [_player pause];
}

- (void)seekToTime:(NSTimeInterval)time {
    [_player setCurrentPlaybackTime:time];
}

- (void)stop {
    [_player stop];
    [self removePlayerTimeObserver];
}

// MARK:  - App lifecycle handlers
- (void)dealloc {
    [_player stop];
    [_player shutdown];
    [_player.view removeFromSuperview];
    _player = nil;
    [self removePlayerTimeObserver];
    [self removePlayerNotificationListeners];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

- (void)removeFromSuperview {
    [_player stop];
    [_player shutdown];
    [_player.view removeFromSuperview];
    _player = nil;
    [self removePlayerTimeObserver];
    [self removePlayerNotificationListeners];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [super removeFromSuperview];
}

- (NSUInteger)degressFromVideoFileWithURL:(NSURL *)url {
    NSUInteger degress = 0;
    AVAsset *asset = [AVAsset assetWithURL:url];
    NSArray *tracks = [asset tracksWithMediaType:AVMediaTypeVideo];
    if([tracks count] > 0) {
        AVAssetTrack *videoTrack = [tracks objectAtIndex:0];
        CGAffineTransform t = videoTrack.preferredTransform;
        if (t.a == 0 && t.b == 1.0 && t.c == -1.0 && t.d == 0){
            // Portrait
            degress = 90;
        } else if(t.a == 0 && t.b == -1.0 && t.c == 1.0 && t.d == 0){
            // PortraitUpsideDown
            degress = 270;
        } else if(t.a == 1.0 && t.b == 0 && t.c == 0 && t.d == 1.0){
            // LandscapeRight
            degress = 0;
        } else if(t.a == -1.0 && t.b == 0 && t.c == 0 && t.d == -1.0){
            // LandscapeLeft
            degress = 180;
        }
    }
    return degress;
}

// MARK: - Delegate Methon
- (void)sendVideoPlayerStatusChanged:(YdkVideoPlayerStatus)status {
    if ([_delegate respondsToSelector:@selector(videoPlayerStatusChanged:)]) {
        [_delegate videoPlayerStatusChanged:status];
    }
}

- (void)sendVideoPlayerFailedWithError:(NSError *)error {
    if ([_delegate respondsToSelector:@selector(videoPlayerFailedWithError:)]) {
        [_delegate videoPlayerFailedWithError:error];
    }
}

@end


