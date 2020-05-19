//
//  YdkTrimmerView.h
//  ydk-trimmer
//
//  Created by yryz on 2019/9/4.
//

#import <UIKit/UIKit.h>
#import <AVKit/AVKit.h>
#import "YdkAssetTimeSelectorView.h"

@protocol YdkTrimmerViewDelegate<NSObject>

- (void)didChangePositionBar:(CMTime)playerTime;
- (void)positionBarStoppedMoving:(CMTime)playerTime;

@end

@interface YdkTrimmerView : YdkAssetTimeSelectorView

@property (nonatomic, strong) UIColor *mainColor;
@property (nonatomic, strong) UIColor *handlerColor;
@property (nonatomic, strong) UIColor *positionBarColor;

@property (nonatomic, assign) CGFloat maxDuration;
@property (nonatomic, assign) CGFloat minDuration;

@property (nonatomic, weak) id<YdkTrimmerViewDelegate> delegate;

- (void)seekToTime:(CMTime)time;
- (CMTime)startTime;
- (CMTime)endTime;

@end
