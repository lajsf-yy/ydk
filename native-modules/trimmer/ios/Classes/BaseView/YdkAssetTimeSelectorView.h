//
//  YdkAssetTimeSelectorView.h
//  ydk-trimmer
//
//  Created by yryz on 2019/9/4.
//

#import <UIKit/UIKit.h>
#import <AVFoundation/AVFoundation.h>

@class YdkAssetVideoScrollView;

@interface YdkAssetTimeSelectorView : UIView <UIScrollViewDelegate>

@property (nonatomic, strong) YdkAssetVideoScrollView *assetPreview;
@property (nonatomic, strong) AVAsset *asset;

@property (nonatomic, assign) CGFloat durationSize;

- (void)setupSubviews;
- (void)constrainAssetPreview;
- (void)assetDidChange:(AVAsset *)newAsset;

- (CMTime)cmTimeFromPosition:(CGFloat)position;
- (CGFloat)positionFromCMTime:(CMTime)cmTime;

@end
