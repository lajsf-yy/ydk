//
//  YdkAssetTimeSelectorView.m
//  ydk-trimmer
//
//  Created by yryz on 2019/9/4.
//

#import "YdkAssetTimeSelectorView.h"
#import "YdkAssetVideoScrollView.h"

#import <ydk-toolkit/YdkToolkit.h>

@implementation YdkAssetTimeSelectorView

- (void)setAsset:(AVAsset *)asset {
    _asset = asset;
    [self layoutIfNeeded];
    [self assetDidChange:asset];
}

- (CGFloat)durationSize {
    return _assetPreview.contentSize.width - 2;
}

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupSubviews];
    }
    return self;
}

- (void)setupSubviews {
    [self setupAssetPrevie];
    [self constrainAssetPreview];
}

- (void)setupAssetPrevie {
    YdkAssetVideoScrollView *assetPreview = [[YdkAssetVideoScrollView alloc] initWithFrame:self.bounds];
    assetPreview.translatesAutoresizingMaskIntoConstraints = NO;
    assetPreview.delegate = self;
    [self addSubview:_assetPreview = assetPreview];
}

- (void)constrainAssetPreview {
    [self addConstraint:[NSLayoutConstraint constraintWithItem:_assetPreview attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeTop multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:_assetPreview attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeLeft multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:_assetPreview attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeBottom multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:_assetPreview attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeRight multiplier:1 constant:0]];
}

- (void)assetDidChange:(AVAsset *)newAsset {
    if (newAsset) {
        [_assetPreview regenerateThumbnailsForAsset:newAsset];
    }
}

- (CMTime)cmTimeFromPosition:(CGFloat)position {
    CMTime cmTime = CMTimeMake(0, 0);
    if (!_asset) {
        return cmTime;
    }
    // 计算出position与总长度的比例
    CGFloat normalizedRatio = MAX(MIN(1, position / [self durationSize]), 0);
    // 根据比例，计算出视频在这一位置下所对应的帧值
    int64_t positionTimeValue = normalizedRatio * _asset.duration.value/*总帧数*/;
    return CMTimeMake(positionTimeValue, _asset.duration.timescale);
}

- (CGFloat)positionFromCMTime:(CMTime)cmTime {
    if (!_asset) {
        return -1;
    }
    // 计算出time与总时长的比例
    CGFloat timeRatio = (cmTime.value * 1.0) * (_asset.duration.timescale * 1.0) / ((_asset.duration.value * 1.0) * (cmTime.timescale * 1.0)); // (当前帧 * 帧每秒) ／ (总帧数 * 帧每秒)
    // 根据比例计算出针对总长度的position
    return timeRatio * [self durationSize];
}

- (void)dealloc {
    DLog(@"%@ 销毁了.", NSStringFromClass(self.class));
}

@end
