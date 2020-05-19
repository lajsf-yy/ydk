//
//  YdkAssetVideoScrollView.m
//  ydk-trimmer
//
//  Created by yryz on 2019/9/4.
//

#import "YdkAssetVideoScrollView.h"

#import <ydk-toolkit/YdkToolkit.h>

@interface YdkAssetVideoScrollView ()

@property (nonatomic, strong) AVAssetImageGenerator *generator;

@end

@implementation YdkAssetVideoScrollView
{
    NSLayoutConstraint *_widthConstraint;
    AVAsset *_asset;
    dispatch_queue_t _generatorQueue;
    BOOL _generateImagesFlag;
}

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        _maxDuration = 10;
        _generatorQueue = dispatch_queue_create("GENERATOR_IMAGE_QUEUE", DISPATCH_QUEUE_CONCURRENT);
        [self setupSubviews];
    }
    return self;
}

- (void)setupSubviews {
    self.backgroundColor = [UIColor clearColor];
    self.showsVerticalScrollIndicator = NO;
    self.showsHorizontalScrollIndicator = NO;
    //    self.clipsToBounds = YES;
    //    self.alwaysBounceHorizontal= YES;
    
    UIView *contentView = [[UIView alloc] init];
    contentView.backgroundColor = [UIColor clearColor];
    contentView.translatesAutoresizingMaskIntoConstraints = NO;
    contentView.tag = -1;
    [self addSubview:_contentView = contentView];
    
    [self addConstraint:[NSLayoutConstraint constraintWithItem:contentView attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeTop multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:contentView attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeLeft multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:contentView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1 constant:0]];
    _widthConstraint = [NSLayoutConstraint constraintWithItem:contentView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeWidth multiplier:1 constant:0];
    [self addConstraint:_widthConstraint];
    _widthConstraint.active = YES;
}

- (void)layoutSubviews {
    [super layoutSubviews];
    self.contentSize = _contentView.bounds.size;
    if (_contentView.subviews.count == 0) {
        [self regenerateThumbnailsForAsset:_asset];
    } else {
        [self updateThumbnailViews];
    }
}

/**
 根据AVAsset生成缩略图
 
 @param asset AVAsset
 */
- (void)regenerateThumbnailsForAsset:(AVAsset *)asset {
    if (!asset) {
        return;
    }
    _asset = asset;
    //    CGSize thumbnailSize = [self thumbnailSizeFromAsset:asset];
    CGSize thumbnailSize = CGSizeMake(fabs((self.frame.size.width - _insetMargin * 2) / _maxDuration), self.frame.size.height);
    if (CGSizeEqualToSize(thumbnailSize, CGSizeZero)) {
        return;
    }
    [_generator cancelAllCGImageGeneration];
    CGSize newContentSize = [self setContentSizeForAsset:asset];
    NSInteger visibleThumbnailsCount = MAX(ceil(self.contentView.frame.size.width / thumbnailSize.width), _maxDuration);
    NSInteger thumbnailCount = round(newContentSize.width / thumbnailSize.width);
    if (thumbnailCount == 0) {
        return;
    }
    [self addThumbnailViews:thumbnailCount size:thumbnailSize];
    NSArray<NSValue *> *timesForThumbnail = [self thumbnailTimeValues:asset thumbnailCount:thumbnailCount];
    [self generateImagesForAsset:asset times:timesForThumbnail maximunSize:thumbnailSize visibleThumbnails:visibleThumbnailsCount];
}

/**
 通过AVAsset获取某一track的图片size，并计算出与自身等比例的size
 
 @param asset AVAsset
 @return 每一帧的size
 */
- (CGSize)thumbnailSizeFromAsset:(AVAsset *)asset {
    AVAssetTrack *track = [asset tracksWithMediaType:AVMediaTypeVideo].firstObject;
    if (!track) {
        return CGSizeZero;
    }
    CGSize assetSize = CGSizeApplyAffineTransform(track.naturalSize, track.preferredTransform);
    
    CGFloat height = self.frame.size.height;
    CGFloat ratio = assetSize.width / assetSize.height;
    CGFloat width = height * ratio;
    return CGSizeMake(fabs(width), fabs(height));
}

/**
 移除前一次记录的缩略图
 */
- (void)removeFormerThumbnails {
    [_contentView.subviews makeObjectsPerformSelector:@selector(removeFromSuperview)];
}

/**
 根据AVAsset设置contentSize
 
 @param asset AVAsset
 @return 实际的contentSize
 */
- (CGSize)setContentSizeForAsset:(AVAsset *)asset {
    int seconds = round(CMTimeGetSeconds(asset.duration));
    CGFloat contentWidthFactor = MAX(1, (seconds / _maxDuration));
    CGFloat originW = (self.frame.size.width - _insetMargin * 2);
    CGFloat contentW = MAX(originW * contentWidthFactor, 0);
    _widthConstraint.active = NO;
    //     _widthConstraint = [NSLayoutConstraint constraintWithItem:_contentView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1 constant:0];
    _widthConstraint = [NSLayoutConstraint constraintWithItem:_contentView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1 constant:contentW];
    _widthConstraint.active = YES;
    //    [self layoutIfNeeded];
    return CGSizeMake(contentW, _contentView.bounds.size.height);
}

/**
 根据缩略图个数和大小，向contentView中添加缩略图
 
 @param count 个数
 @param size 大小
 */
- (void)addThumbnailViews:(NSInteger)count size:(CGSize)size {
    if (_contentView.subviews.count > 0) {
        [self updateThumbnailViews];
        return;
    }
    //    [self removeFormerThumbnails];
    for (NSInteger i = 0; i < count; i++) {
        UIImageView *thumbnailView = [[UIImageView alloc] init];
        thumbnailView.clipsToBounds = YES;
        thumbnailView.contentMode = UIViewContentModeScaleAspectFill;
        thumbnailView.tag = i;
        [self.contentView addSubview:thumbnailView];
    }
    [self updateThumbnailViews];
}

- (void)updateThumbnailViews {
    CGSize thumbnailSize = CGSizeMake(fabs((self.frame.size.width - _insetMargin * 2) / _maxDuration), self.frame.size.height);
    NSInteger i = 0;
    for (UIImageView *imageView in self.contentView.subviews) {
        CGRect frame = CGRectMake(i * thumbnailSize.width, 0, thumbnailSize.width, thumbnailSize.height);
        imageView.frame = frame;
        i ++;
    }
}

/**
 获取视频缩略图时间值
 
 @param asset AVAsset
 @param thumbnailCount 缩图个数
 @return 时间值
 */
- (NSArray<NSValue *> *)thumbnailTimeValues:(AVAsset *)asset thumbnailCount:(NSInteger)thumbnailCount {
    CGFloat seconds = CMTimeGetSeconds(asset.duration);
    CGFloat timeIncrement = (seconds * 1000) / thumbnailCount; // 毫秒间隔
    NSMutableArray *timesForThumbnails = [NSMutableArray array];
    
    for (NSInteger i = 0; i < thumbnailCount; i++) {
        //        CMTime time = CMTimeMakeWithSeconds(i, seconds);
        CMTime time = CMTimeMake(timeIncrement * (i + 0.5), 1000);
        NSValue *value = [NSValue valueWithCMTime:time];
        [timesForThumbnails addObject:value];
    }
    return timesForThumbnails;
}

/**
 生成缩略图image
 
 @param asset AVAsset
 @param times 时间值
 @param maximunSize 最大尺寸
 @param visibleThumbnails 显示缩略图个数
 */
- (void) generateImagesForAsset:(AVAsset *)asset times:(NSArray<NSValue *> *)times maximunSize:(CGSize)maximunSize visibleThumbnails:(NSInteger)visibleThumbnails {
    if (_generateImagesFlag) {
        [self updateThumbnailViews];
        return;
    }
    _generateImagesFlag = YES;
    //    _generator = [AVAssetImageGenerator assetImageGeneratorWithAsset:asset];
    //    _generator.appliesPreferredTrackTransform = YES; // 调整方向
    //    CGSize scaleSize = CGSizeMake(maximunSize.width * [UIScreen mainScreen].scale, maximunSize.height * [UIScreen mainScreen].scale);
    //    _generator.maximumSize = scaleSize;
    //    __block NSInteger index = 0;
    //    __weak typeof(self) weakSelf = self;
    //    AVAssetImageGeneratorCompletionHandler handler = ^(CMTime requestedTime, CGImageRef _Nullable image, CMTime actualTime, AVAssetImageGeneratorResult result, NSError * _Nullable error) {
    //        if (image && !error && result == AVAssetImageGeneratorSucceeded) {
    ////            __strong typeof(weakSelf) strongSelf = weakSelf;
    //            Y_dispatch_main_sync_safe(^{
    //                // 生成图片
    //                if (index == 0) {
    //                    [weakSelf displayFirstImage:image visibleThumbnails:visibleThumbnails];
    //                }
    //                [weakSelf displayImage:image atIndex:index];
    //                index += 1;
    //            });
    //        }
    //    };
    //    [_generator generateCGImagesAsynchronouslyForTimes:times completionHandler:handler];
    
    CGSize scaleSize = CGSizeMake(maximunSize.width * [UIScreen mainScreen].scale, maximunSize.height * [UIScreen mainScreen].scale);
    __block NSInteger index = 0;
    dispatch_async(_generatorQueue, ^{
        for (NSValue *value in times) {
            CMTime time = value.CMTimeValue;
            double second = time.value / time.timescale;
            
            UIImage *image = [YdkVideoTool thumbnailImageFromAsset:asset atTime:second maximunSize:scaleSize];
            dispatch_sync(dispatch_get_main_queue(), ^{
                // 生成图片
                //                if (index == 0) {
                //                    [self displayFirstImage:image visibleThumbnails:visibleThumbnails];
                //                }
                [self displayImage:image atIndex:index];
                index += 1;
            });
        }
    });
}


- (void)displayFirstImage:(UIImage *)image visibleThumbnails:(NSInteger)visibleThumbnails {
    for (NSInteger i = 0; i < visibleThumbnails; i++) {
        [self displayImage:image atIndex:i];
    }
}

- (void)displayImage:(UIImage *)image atIndex:(NSInteger)index {
    UIImageView *imageView = [_contentView viewWithTag:index];
    if (imageView && [imageView isKindOfClass:[UIImageView class]]) {
        //        UIImage *image = [UIImage imageWithCGImage:cgImage scale:1.0 orientation:UIImageOrientationUp];
        imageView.image = image;
    }
}

- (void)dealloc {
    DLog(@"%@ 销毁了.", NSStringFromClass(self.class));
}

@end
