//
//  YdkVideoTrimmerView.m
//  ydk-trimmer
//
//  Created by yryz on 2019/9/4.
//

#import "YdkVideoTrimmerView.h"

#import <ydk-toolkit/YdkToolkit.h>

NSErrorDomain const YdkVideoTrimmerErrorDomain = @"YdkVideoTrimmerErrorDomain";

@interface YdkVideoTrimmerView () <YdkTrimmerViewDelegate>

@property (nonatomic, strong) YdkTrimmerView *trimmerView;
@property (nonatomic, assign, getter=isTrimed) BOOL trimed;

@end

@implementation YdkVideoTrimmerView
{
    UIView *_playerView;
    UIActivityIndicatorView *_indicatorView;
    
    AVPlayer *_player;
    AVPlayerLayer *_playerLayer;
    NSTimer *_playbackTimeCheckerTimer;
}

- (void)setAsset:(AVAsset *)asset {
    _asset = asset;
    [self setupTrimmer:asset];
}

- (void)setMainColor:(UIColor *)mainColor {
    _mainColor = mainColor;
    _trimmerView.mainColor = mainColor;
}

- (instancetype)initWithFrame:(CGRect)frame {
    return [self initWithFrame:frame asset:nil];
}

- (instancetype)initWithCoder:(NSCoder *)aDecoder {
    return [self initWithFrame:CGRectZero asset:nil];
}

- (instancetype)initWithFrame:(CGRect)frame asset:(AVAsset *)asset {
    if (self = [super initWithFrame:frame]) {
        _asset = asset;
        [self setupSubviews:frame];
        [self setupTrimmer:asset];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidEnterBackground:) name:UIApplicationDidEnterBackgroundNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationWillEnterForeground:) name:UIApplicationWillEnterForegroundNotification object:nil];
    }
    return self;
}

- (void)applicationDidEnterBackground:(UIApplication *)application {
    [self stopPlaybackTimeChecker];
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    [_player play];
}

- (void)removeFromSuperview {
    [super removeFromSuperview];
    [self stopPlaybackTimeChecker];
}

- (void)setupSubviews:(CGRect)frame {
    self.backgroundColor = [UIColor blackColor];
    // 为避免约束警告log加了初始frame
    YdkTrimmerView *trimmerView = [[YdkTrimmerView alloc] initWithFrame:CGRectMake(0, frame.size.height - 10 * 2 - 50, frame.size.width, 50)];
    trimmerView.delegate = self;
    [self addSubview:_trimmerView = trimmerView];
    
    UIView *playerView = [[UIView alloc] init];
    [self addSubview:_playerView = playerView];
}

- (void)setupTrimmer:(AVAsset *)asset {
    if (!asset) {
        return;
    }
    _trimmerView.asset = asset;
    [self addVideoPlayer:asset];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    CGFloat width = self.frame.size.width;
    CGFloat height = self.frame.size.height;
    CGFloat margin = 10;
    CGFloat trimmerH = 50;
    
    CGFloat trimmerX = margin * 3;
    CGFloat trimmerY = height - /*buttonH - */margin * 2 - trimmerH;
    _trimmerView.frame = CGRectMake(0, trimmerY, width, trimmerH);
    
    CGFloat playerX = margin * 2;
    CGFloat playerY = 5;
    _playerView.frame = CGRectMake(playerX, playerY, width - playerX * 2, trimmerY - margin - margin);
    _playerLayer.frame = CGRectMake(0, 0, _playerView.frame.size.width, _playerView.frame.size.height);
}

// MARK: - Action
- (void)cancel {
    [self stopPlaybackTimeChecker];
}

- (void)trim:(void (^)(NSError *error, YdkVideoInfo *video))complete {
    if (self.isTrimed) {
        return;
    }
    _trimed = YES;
    [self stopPlaybackTimeChecker];
    CMTime start = _trimmerView.startTime;
    CMTime end = _trimmerView.endTime;
    TK_WEAK_SELF(weakSelf);
    [self prepareAssetComposition:start endTime:end complete:^(NSURL *url, NSError *error) {
        if (complete) {
            if (!error) {
                UIImage *thumbnailImage = [YdkVideoTool thumbnailImageFromVideoURL:url];
                NSURL *thumbnailURL = [YdkFileSystemTool createCacheFileURLWithComponent:NSStringFromClass(self.class) fileName:@"YTrimmedMovieThumbnail_tmp.jpg"];
                BOOL result = [YdkImageTool saveImage:thumbnailImage targetURL:thumbnailURL];
                if (result) {
                    YdkVideoInfo *videoInfo = [YdkVideoInfo videoInfoWithVideoURL:url thumbnailURL:thumbnailURL];
                    complete(nil, videoInfo);
                } else {
                    NSError *error = [NSError errorWithDomain:YdkVideoTrimmerErrorDomain code:YdkVideoTrimmerErrorSaveVideoThumbnailFailed userInfo:@{NSLocalizedDescriptionKey : @"保存视频缩略图失败"}];
                    complete(error, nil);
                }
            } else {
                complete(error, nil);
            }
        }
        if (weakSelf.saveToPhotoAlbum && url) {
            [weakSelf saveToPhotoAlbum:url];
        }
        weakSelf.trimed = NO;
    }];
}

// MARK: - Trimmer
- (void)addVideoPlayer:(AVAsset *)asset {
    AVPlayerItem *playerItem = [AVPlayerItem playerItemWithAsset:asset];
    _player = [AVPlayer playerWithPlayerItem:playerItem];
    [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(itemDidFinishPlaying:) name:AVPlayerItemDidPlayToEndTimeNotification object:playerItem];
    
    AVPlayerLayer *layer = [AVPlayerLayer playerLayerWithPlayer:_player];
    layer.backgroundColor = [UIColor clearColor].CGColor;
    layer.videoGravity = AVLayerVideoGravityResizeAspect;
    [_playerView.layer.sublayers makeObjectsPerformSelector:@selector(removeFromSuperlayer)];
    [_playerView.layer addSublayer:_playerLayer = layer];
    [self startPlaybackTimeChecker];
}

- (void)itemDidFinishPlaying:(NSNotification *)nofi {
    CMTime startTime = [_trimmerView startTime];
    if (CMTimeCompare(startTime, CMTimeMake(0, 0)) != 0) {
        [_player seekToTime:startTime];
        [_player play];
    }
}

- (void)startPlaybackTimeChecker {
    [self stopPlaybackTimeChecker];
     _playbackTimeCheckerTimer = [NSTimer scheduledTimerWithTimeInterval:1 / 60 target:self selector:@selector(onPlaybackTimeChecker) userInfo:nil repeats:YES];
     [[NSRunLoop currentRunLoop] addTimer:_playbackTimeCheckerTimer forMode:NSRunLoopCommonModes];
    [_player play];
}

- (void)stopPlaybackTimeChecker {
    [_playbackTimeCheckerTimer invalidate];
    _playbackTimeCheckerTimer = nil;
    [_player pause];
}

- (void)onPlaybackTimeChecker {
    CMTime startTime = [_trimmerView startTime];
    CMTime stopTime = [_trimmerView endTime];
    if (CMTimeCompare(startTime, CMTimeMake(0, 0)) != 0 && CMTimeCompare(stopTime, CMTimeMake(0, 0)) != 0 && _player) {
        CMTime playBackTime = [_player currentTime];
        //        DLog(@"onPlaybackTime: %.2f", playBackTime.value * 1.0 / playBackTime.timescale);
        [_trimmerView seekToTime:playBackTime];
        int32_t result = CMTimeCompare(playBackTime, stopTime);
        if ( result == 1 || result == 0) {
            [_player seekToTime:startTime toleranceBefore:kCMTimeZero toleranceAfter:kCMTimeZero];
            [_trimmerView seekToTime:startTime];
        }
    }
}

- (void)saveToPhotoAlbum:(NSURL *)url {
    UISaveVideoAtPathToSavedPhotosAlbum(url.path, nil, nil, nil);
}

// MARK: - Video Composition
- (void)prepareAssetComposition:(CMTime)start endTime:(CMTime)end complete:(nonnull void(^)(NSURL *url, NSError *error))complete {
    if (!_asset) {
        complete(nil, [NSError errorWithDomain:YdkVideoTrimmerErrorDomain code:YdkVideoTrimmerErrorInvalidAVAsset userInfo:@{NSLocalizedDescriptionKey : @"无效的AVAsset."}]);
        return;
    }
    
    // video track
    AVAssetTrack *videoTrack = [_asset tracksWithMediaType:AVMediaTypeVideo].firstObject;
    if (!videoTrack) {
        complete(nil, [NSError errorWithDomain:YdkVideoTrimmerErrorDomain code:YdkVideoTrimmerErrorInvalidAVAssetTrack userInfo:@{NSLocalizedDescriptionKey : @"无效的video AVAssetTrack对象"}]);
        return;
    }
    AVMutableComposition *assetComposition = [[AVMutableComposition alloc] init];
    AVMutableCompositionTrack *videoCompositionTrack = [assetComposition addMutableTrackWithMediaType:AVMediaTypeVideo preferredTrackID:kCMPersistentTrackID_Invalid];
    if (!videoCompositionTrack) {
        complete(nil, [NSError errorWithDomain:YdkVideoTrimmerErrorDomain code:YdkVideoTrimmerErrorInvalidAVMutableCompositionTrack userInfo:@{NSLocalizedDescriptionKey : @"无效的video AVMutableCompositionTrack对象"}]);
        return;
    }
    
    CMTimeRange timeRange = CMTimeRangeFromTimeToTime(start, end);
    NSError *error;
    [videoCompositionTrack insertTimeRange:timeRange ofTrack:videoTrack atTime:kCMTimeZero error:&error];
    if (error) {
        complete(nil, error);
        return;
    }
    
    // audio track
    AVAssetTrack *audioTrack = [_asset tracksWithMediaType:AVMediaTypeAudio].firstObject;
    if (!audioTrack) {
        complete(nil, [NSError errorWithDomain:YdkVideoTrimmerErrorDomain code:YdkVideoTrimmerErrorInvalidAVAssetTrack userInfo:@{NSLocalizedDescriptionKey : @"无效的audio AVAssetTrack对象"}]);
        return;
    }
    AVMutableCompositionTrack *audioCompositionTrack = [assetComposition addMutableTrackWithMediaType:AVMediaTypeAudio preferredTrackID:kCMPersistentTrackID_Invalid];
    if (!audioCompositionTrack) {
        complete(nil, [NSError errorWithDomain:YdkVideoTrimmerErrorDomain code:YdkVideoTrimmerErrorInvalidAVMutableCompositionTrack userInfo:@{NSLocalizedDescriptionKey : @"无效的audio AVMutableCompositionTrack对象"}]);
        return;
    }
    [audioCompositionTrack insertTimeRange:timeRange ofTrack:audioTrack atTime:kCMTimeZero error:&error];
    if (error) {
        complete(nil, error);
        return;
    }
    
    // 1. AVMutableVideoCompositionInstruction
    AVMutableVideoCompositionInstruction *mainInstructions = [[AVMutableVideoCompositionInstruction alloc] init];
    mainInstructions.timeRange = CMTimeRangeMake(kCMTimeZero, _asset.duration);
    // 2. AVMutableVideoCompositionLayerInstruction
    AVMutableVideoCompositionLayerInstruction *layerInstructions = [AVMutableVideoCompositionLayerInstruction videoCompositionLayerInstructionWithAssetTrack:videoCompositionTrack];
    // 视频设备旋转问题
    CGFloat ratation = atan2(videoTrack.preferredTransform.b, videoTrack.preferredTransform.a);
    CGPoint rotationOffset = CGPointZero;
    if (videoTrack.preferredTransform.b == -1.0) {
        rotationOffset.y = videoTrack.naturalSize.width;
    } else if (videoTrack.preferredTransform.c == -1.0) {
        rotationOffset.x = videoTrack.naturalSize.height;
    } else if (videoTrack.preferredTransform.a == -1.0) {
        rotationOffset.x = videoTrack.naturalSize.width;
        rotationOffset.y = videoTrack.naturalSize.height;
    }
    CGAffineTransform transform = CGAffineTransformIdentity;
    transform = CGAffineTransformTranslate(transform, rotationOffset.x, rotationOffset.y);
    transform = CGAffineTransformRotate(transform, ratation);
    [layerInstructions setTransform:transform atTime:kCMTimeZero];
    
    [layerInstructions setOpacity:1.0 atTime:kCMTimeZero];
    mainInstructions.layerInstructions = @[layerInstructions];
    // 3. AVMutableVideoComposition
    AVMutableVideoComposition *videoComposition = [[AVMutableVideoComposition alloc] init];
    CGSize assetSize = CGSizeApplyAffineTransform(videoTrack.naturalSize, videoTrack.preferredTransform);   //
    videoComposition.renderSize = CGSizeMake(ABS(assetSize.width), ABS(assetSize.height));
    videoComposition.instructions = @[mainInstructions];
    videoComposition.frameDuration = CMTimeMake(1, 30);
    
    // 4. AVAssetExportSession
    AVAssetExportSession *exportSession = [AVAssetExportSession exportSessionWithAsset:assetComposition presetName:AVAssetExportPresetHighestQuality];
    if (!exportSession) {
        complete(nil, [NSError errorWithDomain:YdkVideoTrimmerErrorDomain code:YdkVideoTrimmerErrorInvalidAVAssetExportSession userInfo:@{NSLocalizedDescriptionKey : @"无效的AVAssetExportSession对象"}]);
        return;
    }
    [exportSession setOutputFileType:AVFileTypeMPEG4];
    exportSession.shouldOptimizeForNetworkUse = YES;
    exportSession.videoComposition = videoComposition;
    NSURL *fileURL = [YdkFileSystemTool createCacheFileURLWithComponent:NSStringFromClass(self.class) fileName:@"YTrimmedMovie_tmp.mp4"];
    exportSession.outputURL = fileURL;
    [self startActivityIndicatorAnimating];
    __weak typeof(self) weakSelf = self;
    [exportSession exportAsynchronouslyWithCompletionHandler:^{
        dispatch_async(dispatch_get_main_queue(), ^{
            [weakSelf stopActivityIndicatorAnimating];
            NSURL *outputURL = exportSession.outputURL;
            if (outputURL && exportSession.status == AVAssetExportSessionStatusCompleted) {
                complete(outputURL, nil);
            } else {
                complete(nil, exportSession.error);
            }
        });
    }];
}

// MARK: - UIActivityIndicatorView
- (void)startActivityIndicatorAnimating {
    if (_indicatorView == nil) {
        _indicatorView = [[UIActivityIndicatorView alloc] initWithActivityIndicatorStyle:UIActivityIndicatorViewStyleWhiteLarge];
        [self insertSubview:_indicatorView atIndex:0];
        [self bringSubviewToFront:_indicatorView];
        _indicatorView.center = self.center;
    }
    [_indicatorView startAnimating];
}

- (void)stopActivityIndicatorAnimating {
    [_indicatorView stopAnimating];
}

// MARK: - Trimmer Delegate
- (void)positionBarStoppedMoving:(CMTime)playerTime {
    [_player seekToTime:playerTime toleranceBefore:kCMTimeZero toleranceAfter:kCMTimeZero];
    [_player play];
    [self startPlaybackTimeChecker];
}

- (void)didChangePositionBar:(CMTime)playerTime {
    [self stopPlaybackTimeChecker];
    [_player seekToTime:playerTime toleranceBefore:kCMTimeZero toleranceAfter:kCMTimeZero];
}

- (void)dealloc {
    DLog(@"%@ 销毁了.", NSStringFromClass(self.class));
    [[NSNotificationCenter defaultCenter] removeObserver:self];
}

@end
