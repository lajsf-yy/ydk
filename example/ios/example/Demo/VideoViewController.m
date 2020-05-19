//
//  VideoViewController.m
//  example
//
//  Created by yryz on 2019/11/5.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "VideoViewController.h"

#import <ydk-toolkit/YdkToolkit.h>
#import <ydk-video/YdkVideoPlayerView.h>
#import <ydk-audio/YdkAudioPlayerManager.h>

@interface VideoViewController () <YdkVideoPlayerDelegate, YdkAudioPlayerDelegate>

@property (weak, nonatomic) IBOutlet YdkVideoPlayerView *playerView;
@property (weak, nonatomic) IBOutlet UISlider *slider;
@property (weak, nonatomic) IBOutlet UILabel *currentLabel;
@property (weak, nonatomic) IBOutlet UILabel *durationLabel;
@property (weak, nonatomic) IBOutlet UIButton *playerButton;
@property (weak, nonatomic) IBOutlet UIActivityIndicatorView *indicator;

@end

@implementation VideoViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    
    //    NSDictionary *source = @{ @"uri" : @"https://yryz-resources-mo.oss-cn-hangzhou.aliyuncs.com/audio/opus/254C8020-37B1-44D0-8A9D-86089F00CD87_iOS.mp3" };
//    NSDictionary *source = @{ @"uri" : @"https://cdn-test.lajsf.com/nutrition-plan/video/default/201909/461587190988800.mp4" };
    NSDictionary *source = @{ @"uri" : @"https://cdn-s.lajsf.com/nutrition-plan/video/default/201910/472412805160960.mp4?auth_key=1571111455962-97-0-a6febd827f28b6238923df1357b5c4fa" };
    [_playerView setSource:source];
    _playerView.delegate = self;
}

- (IBAction)forward15s:(id)sender {
    //    [_playerView seekToTime:MAX(_playerView.currentPlaybackTime - 15, 0)];
    if (_playerView.isPlaying) {
        [_playerView pause];
    } else {
        [_playerView play];
    }
}

- (IBAction)backward15s:(id)sender {
    [_playerView seekToTime:MIN(_playerView.currentPlaybackTime + 15, _playerView.duration)];
}

- (IBAction)play:(id)sender {
    //    if (_playerView.isPlaying) {
    //        [_playerView pause];
    //    } else {
    //        [_playerView play];
    //    }
    //    NSURL *url = [NSURL URLWithString:@"https://yryz-resources-mo.oss-cn-hangzhou.aliyuncs.com/audio/opus/254C8020-37B1-44D0-8A9D-86089F00CD87_iOS.mp3"];
    //    NSURL *url = [NSURL URLWithString:@"https://cdn-test.lajsf.com/nutrition-plan/video/default/201909/461587190988800.mp4"];
    NSURL *url = [NSURL URLWithString:@"https://cdn-s.lajsf.com/nutrition-plan/video/default/201910/472412805160960.mp4?auth_key=1571111455962-97-0-a6febd827f28b6238923df1357b5c4fa"];
    [YdkAudioPlayerManager playWithURL:url tagId:@(1) delegate:self];
}

- (void)audioPlayerUpdateProgress:(Float64)progress duration:(Float64)duration playableDuration:(Float64)playableDuration {
    DLog(@"audioPlayerUpdateProgress%.0f, %.0f", progress, duration);
}

// MARK: - YdkVideoPlayerDelegate

- (void)videoPlayerStatusChanged:(YdkVideoPlayerStatus)status {
    NSString *msg = nil;
    switch (status) {
        case YdkVideoPlayerStatusReadyToPlay:
            msg = @"准备就绪";
            break;
        case YdkVideoPlayerStatusPlay:
            msg = @"开始播放";
            break;
        case YdkVideoPlayerStatusPause:
            msg = @"暂停";
            break;
        case YdkVideoPlayerStatusLoading: {
            [_indicator startAnimating];
            msg = @"加载中...";
        }
            break;
        case YdkVideoPlayerStatusStop:
            msg = @"停止";
            break;
        case YdkVideoPlayerStatusLoaded: {
            [_indicator stopAnimating];
            msg = @"加载完成";
        }
            break;
        default:
            break;
    }
    DLog(@"videoPlayerStatusChanged: %@", msg);
}

- (void)videoPlayerUpdateProgress:(Float64)progress duration:(Float64)duration playableDuration:(Float64)playableDuration {
    _currentLabel.text = [NSString stringWithFormat:@"%02d:%02d", (int)progress / 60, (int)progress % 60];
    _durationLabel.text = [NSString stringWithFormat:@"%02d:%02d", (int)duration / 60, (int)duration % 60];
    _slider.value = progress / duration;
}

- (void)videoPlayerFailedWithError:(NSError *)error {
    DLog(@"Error: %@", error);
}


@end
