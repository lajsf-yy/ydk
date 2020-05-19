//
//  YdkVideoPlayerReactView.h
//  ydk-video
//
//  Created by yryz on 2019/7/15.
//

#import <React/RCTView.h>
#import <React/RCTEventDispatcher.h>

#import "YdkVideoPlayerView.h"

@interface YdkVideoPlayerReactView : RCTView

@property (readonly, nonatomic, strong) YdkVideoPlayerView *videoView;

@property (nonatomic, copy) RCTDirectEventBlock onReadyToPlay;
@property (nonatomic, copy) RCTDirectEventBlock onVideoLoad;
@property (nonatomic, copy) RCTDirectEventBlock onVideoLoadEnd;
@property (nonatomic, copy) RCTDirectEventBlock onPlayEnd;
@property (nonatomic, copy) RCTDirectEventBlock onVideoProgress;
@property (nonatomic, copy) RCTDirectEventBlock onPlayError;

- (instancetype)initWithEventDispatcher:(RCTEventDispatcher *)eventDispatcher NS_DESIGNATED_INITIALIZER;

@end
