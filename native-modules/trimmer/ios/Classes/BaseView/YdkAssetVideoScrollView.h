//
//  YdkAssetVideoScrollView.h
//  ydk-trimmer
//
//  Created by yryz on 2019/9/4.
//

#import <UIKit/UIKit.h>
#import <AVKit/AVKit.h>

@interface YdkAssetVideoScrollView : UIScrollView

@property (nonatomic, strong) UIView *contentView;

@property (nonatomic, assign) double maxDuration;
@property (nonatomic, assign) CGFloat insetMargin;

- (void)regenerateThumbnailsForAsset:(AVAsset *)asset;

@end
