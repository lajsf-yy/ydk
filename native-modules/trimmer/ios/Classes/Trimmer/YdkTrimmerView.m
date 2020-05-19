//
//  YdkTrimmerView.m
//  ydk-trimmer
//
//  Created by yryz on 2019/9/4.
//

#import "YdkTrimmerView.h"
#import "YdkHandlerView.h"
#import "YdkAssetVideoScrollView.h"

#import <ydk-toolkit/YdkToolkit.h>

static const CGFloat kHandleWidth = 8;
static const CGFloat kPositionBarWidth = 2;
static const CGFloat kMargin = 10 * 3;

@interface YdkTrimmerView ()

@property (nonatomic, strong) UIView *positionBarView;

@property (nonatomic, strong) UIView *trimView;
@property (nonatomic, strong) YdkHandlerView *leftHandleView;
@property (nonatomic, strong) YdkHandlerView *rightHandleView;
@property (nonatomic, strong) UIView *positionBar;
@property (nonatomic, strong) UIView *leftMaskView;
@property (nonatomic, strong) UIView *rightMaskView;

@property (nonatomic, assign) CGFloat currentLeftConstraint;
@property (nonatomic, assign) CGFloat currentRightConstraint;
@property (nonatomic, strong) NSLayoutConstraint *leftConstraint;
@property (nonatomic, strong) NSLayoutConstraint *rightConstraint;
@property (nonatomic, strong) NSLayoutConstraint *positionConstraint;

@end

@implementation YdkTrimmerView {
    struct TrimmerViewDelegate {
        unsigned int didChangePositionBar       : 1;
        unsigned int positionBarStoppedMoving   : 1;
    } _trimmerViewDelegateFlags;
    
    BOOL _beginDragging;
}

- (void)setMainColor:(UIColor *)mainColor {
    _mainColor = mainColor;
    [self updateMainColor];
}

- (void)setHandlerColor:(UIColor *)handlerColor {
    _handlerColor = handlerColor;
    [self updateHandleColor];
}

- (void)setPositionBarColor:(UIColor *)positionBarColor {
    _positionBarColor = positionBarColor;
    _positionBar.backgroundColor = positionBarColor;
}

- (void)setMaxDuration:(CGFloat)maxDuration {
    _maxDuration = MAX(1, maxDuration);
    self.assetPreview.maxDuration = maxDuration;
}

- (void)setMinDuration:(CGFloat)minDuration {
    _minDuration = MAX(0, minDuration);
}

- (void)setDelegate:(id<YdkTrimmerViewDelegate>)delegate {
    _delegate = delegate;
    _trimmerViewDelegateFlags.didChangePositionBar = [delegate respondsToSelector:@selector(didChangePositionBar:)];
    _trimmerViewDelegateFlags.positionBarStoppedMoving = [delegate respondsToSelector:@selector(positionBarStoppedMoving:)];
}

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        
    }
    return self;
}

// MARK: - override
- (void)setupSubviews {
    [super setupSubviews];
    self.backgroundColor = [UIColor clearColor];
    self.layer.zPosition = 1.0;
    
    _maxDuration = 10;
    _minDuration = 1;
    _mainColor = [UIColor colorWithWhite:0.95 alpha:1];
    _handlerColor = [UIColor colorWithWhite:0.8 alpha:1];
    _positionBarColor = [UIColor colorWithWhite:1.0 alpha:0.5];
    
    [self setupTrimmerView];
    [self setupHandleView];
    [self setupMaskView];
    [self setupPositionBar];
    [self setupGestures];
    [self updateMainColor];
    [self updateHandleColor];
    
    CGFloat insetMargin = kMargin + kHandleWidth;
    self.assetPreview.contentInset = UIEdgeInsetsMake(0, insetMargin, 0, insetMargin);
    self.assetPreview.insetMargin = insetMargin;
}

- (void)constrainAssetPreview {
    [self addConstraint:[NSLayoutConstraint constraintWithItem:self.assetPreview attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeTop multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:self.assetPreview attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeLeft multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:self.assetPreview attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeBottom multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:self.assetPreview attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeRight multiplier:1 constant:0]];
    
    //    [self addConstraint:[NSLayoutConstraint constraintWithItem:self.assetPreview attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterX multiplier:1 constant:0]];
    //    [self addConstraint:[NSLayoutConstraint constraintWithItem:self.assetPreview attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterY multiplier:1 constant:0]];
    //    [self addConstraint:[NSLayoutConstraint constraintWithItem:self.assetPreview attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1 constant:0]];
    //    [self addConstraint:[NSLayoutConstraint constraintWithItem:self.assetPreview attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1 constant:[UIScreen mainScreen].bounds.size.width]];
}

//- (void)layoutSubviews {
//    [super layoutSubviews];
//    CGFloat insetMargin = kMargin + kHandleWidth;
//    self.assetPreview.contentInset = UIEdgeInsetsMake(0, insetMargin, 0, insetMargin);
//    self.assetPreview.insetMargin = insetMargin;
//}

- (void)setupTrimmerView {
    UIView *trimView = [[UIView alloc] init];
    trimView.layer.borderWidth = 1.0;
    trimView.layer.cornerRadius = 2.0;
    trimView.translatesAutoresizingMaskIntoConstraints = NO;
    trimView.userInteractionEnabled = NO;
    [self addSubview:_trimView = trimView];
    
    [self addConstraint:[NSLayoutConstraint constraintWithItem:trimView attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeTop multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:trimView attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeBottom multiplier:1 constant:0]];
    _leftConstraint = [NSLayoutConstraint constraintWithItem:trimView attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeLeft multiplier:1 constant:kMargin];
    _rightConstraint = [NSLayoutConstraint constraintWithItem:trimView attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeRight multiplier:1 constant:-kMargin];
    [self addConstraint:_leftConstraint];
    [self addConstraint:_rightConstraint];
    _leftConstraint.active = YES;
    _rightConstraint.active = YES;
}

- (void)setupHandleView {
    // left
    YdkHandlerView *leftHandleView = [[YdkHandlerView alloc] init];
    leftHandleView.userInteractionEnabled = YES;
    //    leftHandleView.layer.cornerRadius = 2.0;
    leftHandleView.translatesAutoresizingMaskIntoConstraints = NO;
    leftHandleView.knobColor = _handlerColor;
    [self addSubview:_leftHandleView = leftHandleView];
    
    [self addConstraint:[NSLayoutConstraint constraintWithItem:leftHandleView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:leftHandleView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1 constant:kHandleWidth]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:leftHandleView attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self.trimView attribute:NSLayoutAttributeLeft multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:leftHandleView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterY multiplier:1 constant:0]];
    
    // right
    YdkHandlerView *rightHandleView = [[YdkHandlerView alloc] init];
    rightHandleView.userInteractionEnabled = YES;
    //    rightHandleView.layer.cornerRadius = 2.0;
    rightHandleView.translatesAutoresizingMaskIntoConstraints = NO;
    leftHandleView.knobColor = _handlerColor;
    [self addSubview:_rightHandleView = rightHandleView];
    
    [self addConstraint:[NSLayoutConstraint constraintWithItem:rightHandleView attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:rightHandleView attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:1 constant:kHandleWidth]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:rightHandleView attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self.trimView attribute:NSLayoutAttributeRight multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:rightHandleView attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterY multiplier:1 constant:0]];
}

- (void)setupMaskView {
    // left
    UIView *leftMaskView = [[UIView alloc] init];
    leftMaskView.userInteractionEnabled = false;
    leftMaskView.backgroundColor = [UIColor blackColor];
    leftMaskView.alpha = 0.5;
    leftMaskView.translatesAutoresizingMaskIntoConstraints = NO;
    [self insertSubview:_leftMaskView = leftMaskView belowSubview:_leftHandleView];
    
    [self addConstraint:[NSLayoutConstraint constraintWithItem:leftMaskView attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeLeft multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:leftMaskView attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeBottom multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:leftMaskView attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeTop multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:leftMaskView attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self.leftHandleView attribute:NSLayoutAttributeCenterX multiplier:1 constant:0]];
    // right
    UIView *rightMaskView = [[UIView alloc] init];
    rightMaskView.userInteractionEnabled = false;
    rightMaskView.backgroundColor = [UIColor blackColor];
    rightMaskView.alpha = 0.5;
    rightMaskView.translatesAutoresizingMaskIntoConstraints = NO;
    [self insertSubview:_rightMaskView = rightMaskView belowSubview:_rightHandleView];
    
    [self addConstraint:[NSLayoutConstraint constraintWithItem:rightMaskView attribute:NSLayoutAttributeRight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeRight multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:rightMaskView attribute:NSLayoutAttributeBottom relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeBottom multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:rightMaskView attribute:NSLayoutAttributeTop relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeTop multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:rightMaskView attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self.rightHandleView attribute:NSLayoutAttributeCenterX multiplier:1 constant:0]];
}

- (void)setupPositionBar {
    UIView *positionBar = [[UIView alloc] init];
    positionBar.frame = CGRectMake(0, 0, kPositionBarWidth, CGRectGetHeight(self.frame));
    positionBar.backgroundColor = _positionBarColor;
    positionBar.center = CGPointMake(CGRectGetMaxX(_leftHandleView.frame), self.center.y);
    //    positionBar.layer.cornerRadius = 1;
    positionBar.translatesAutoresizingMaskIntoConstraints = NO;
    positionBar.userInteractionEnabled = NO;
    [self addSubview:_positionBar = positionBar];
    
    [self addConstraint:[NSLayoutConstraint constraintWithItem:positionBar attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterY multiplier:1 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:positionBar attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:0 constant:kPositionBarWidth]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:positionBar attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1 constant:0]];
    _positionConstraint = [NSLayoutConstraint constraintWithItem:positionBar attribute:NSLayoutAttributeLeft relatedBy:NSLayoutRelationEqual toItem:self.leftHandleView attribute:NSLayoutAttributeRight multiplier:1 constant:0];
    [self addConstraint:_positionConstraint];
    _positionConstraint.active = YES;
}

- (void)setupGestures {
    UIPanGestureRecognizer *leftPGR = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handlePanGesture:)];
    [_leftHandleView addGestureRecognizer:leftPGR];
    UIPanGestureRecognizer *rightPGR = [[UIPanGestureRecognizer alloc] initWithTarget:self action:@selector(handlePanGesture:)];
    [_rightHandleView addGestureRecognizer:rightPGR];
}

- (void)updateMainColor {
    _trimView.layer.borderColor = _mainColor.CGColor;
    _leftHandleView.backgroundColor = _mainColor;
    _rightHandleView.backgroundColor = _mainColor;
}

- (void)updateHandleColor {
    _leftHandleView.knobColor = _handlerColor;
    _rightHandleView.knobColor = _handlerColor;
}

// MARK: - Trim Gestures
- (void)handlePanGesture:(UIPanGestureRecognizer *)gestureRecognizer {
    if (!gestureRecognizer.view || !gestureRecognizer.view.superview) {
        return;
    }
    BOOL isLeft = gestureRecognizer.view == _leftHandleView;
    switch (gestureRecognizer.state) {
        case UIGestureRecognizerStateBegan: {
            if (isLeft) {
                _currentLeftConstraint = _leftConstraint.constant;
            } else {
                _currentRightConstraint = _rightConstraint.constant;
            }
            [self updateSelectedTime:NO];
        }
            break;
        case UIGestureRecognizerStateChanged: {
            CGPoint translation = [gestureRecognizer translationInView:gestureRecognizer.view.superview];
            if (isLeft) {
                [self updateLeftConstraint:translation];
            } else {
                [self updateRightConstraint:translation];
            }
            [self layoutIfNeeded];
            CMTime startTime = [self startTime];
            CMTime stopTime = [self endTime];
            if (isLeft && CMTimeCompare(startTime, CMTimeMake(0, 0)) != 0) {
                [self seekToTime:startTime];
            } else if (CMTimeCompare(stopTime, CMTimeMake(0, 0)) != 0) {
                [self seekToTime:stopTime];
            }
            [self updateSelectedTime:NO];
        }
            break;
        case UIGestureRecognizerStateCancelled:
        case UIGestureRecognizerStateEnded:
        case UIGestureRecognizerStateFailed:
            [self seekToTime:[self startTime]];
            [self updateSelectedTime:YES];
            break;
        default:
            break;
    }
}

/**
 根据当前手势位置获取leftHandleView的约束常量值
 
 @param point 当前手势位置
 */
- (void)updateLeftConstraint:(CGPoint)point {
    CGFloat maxConstraint = MAX(_rightHandleView.frame.origin.x - kHandleWidth - [self minimumDistanceBetweenHandle], 0);
    CGFloat newConstraint = MIN(MAX(kMargin, _currentLeftConstraint + point.x), maxConstraint);
    _leftConstraint.constant = newConstraint;
}

- (void)updateRightConstraint:(CGPoint)point {
    CGFloat maxConstraint = MIN(2 * kHandleWidth - self.frame.size.width + _leftHandleView.frame.origin.x + [self minimumDistanceBetweenHandle], 0);
    CGFloat newConstraint = MAX(MIN(-kMargin, _currentRightConstraint + point.x), maxConstraint);
    _rightConstraint.constant = newConstraint;
}

// MARK: - Asset loading

// override
- (void)assetDidChange:(AVAsset *)newAsset {
    [super assetDidChange:newAsset];
    [self resetHandleViewPosition];
}

- (void)resetHandleViewPosition {
    _leftConstraint.constant = kMargin;
    _rightConstraint.constant = -kMargin;
    [self layoutIfNeeded];
}

// MARK: - Time Equivalence

/**
 移动positionBar位置
 
 @param time 跳转到的时间值
 */
- (void)seekToTime:(CMTime)time {
    CGFloat newPosition = [self positionFromCMTime:time];
    if (newPosition > -1) {
        CGFloat offset = (self.assetPreview.contentOffset.x >= 0 ? 0 : _leftHandleView.frame.size.width);
        CGFloat offsetPosition = newPosition - self.assetPreview.contentOffset.x - (_leftHandleView.frame.origin.x + offset);
        CGFloat maxPosition = _rightHandleView.frame.origin.x - (_leftHandleView.frame.origin.x + kHandleWidth) - _positionBar.frame.size.width;
        CGFloat normalPosition = MIN(MAX(0, offsetPosition), maxPosition);
        _positionConstraint.constant = normalPosition;
        //        DLog(@"Time: %.2f, newPosition: %.f, offsetPosition: %.f, normalPosition: %.f", time.value * 1.0 / time.timescale, newPosition, offsetPosition, normalPosition);
//        [self layoutIfNeeded];
    }
}

/**
 选中区域的开始时间
 
 @return 开始时间的CMTime
 */
- (CMTime)startTime {
    CGFloat startPosition = _leftHandleView.frame.origin.x + self.assetPreview.contentOffset.x;
    return [self cmTimeFromPosition:startPosition];
}

- (CMTime)endTime {
    CGFloat offset = self.assetPreview.contentOffset.x < 0 ? kPositionBarWidth : 0;
    CGFloat stopPosition = _rightHandleView.frame.origin.x + self.assetPreview.contentOffset.x - offset;
    return [self cmTimeFromPosition:stopPosition];
}

/**
 实时回调选中的时间CMTime
 
 @param stoppedMoving 是否停止移动
 */
- (void)updateSelectedTime:(BOOL)stoppedMoving {
    CMTime playerTime = [self positionBarTime];
    if (CMTimeCompare(playerTime, CMTimeMake(0, 0)) == 0) {
        return;
    }
    if (stoppedMoving) {
        if (_trimmerViewDelegateFlags.positionBarStoppedMoving) {
            [_delegate positionBarStoppedMoving:playerTime];
        }
    } else {
        if (_trimmerViewDelegateFlags.didChangePositionBar) {
            [_delegate didChangePositionBar:playerTime];
        }
    }
}

/**
 根据positionBar的位置获取对应的CMTime
 
 @return CMTime
 */
- (CMTime)positionBarTime {
    CGFloat barPosition = _positionBar.frame.origin.x + 0 + self.assetPreview.contentOffset.x + (self.assetPreview.contentOffset.x >= 0 ? -_leftHandleView.frame.size.width : 0);
    return [self cmTimeFromPosition:barPosition];
}

/**
 获取两个HandleView之间最小距离
 
 @return 最小距离值
 */
- (CGFloat)minimumDistanceBetweenHandle {
    if (!self.asset) {
        return 0;
    } else {
        NSInteger seconds = ceil((self.asset.duration.value * 1.0f) / (self.asset.duration.timescale * 1.0));
        return _minDuration * self.assetPreview.contentView.frame.size.width / seconds;
    }
}

// MARK: - Hit Test 放大HandleView响应区域
- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    CGRect hitFrame = CGRectInset(self.bounds, -10, -20);
    if (CGRectContainsPoint(hitFrame, point)) {
        UIView *leftH = [_leftHandleView hitTest:[self convertPoint:point toView:_leftHandleView] withEvent:event];
        UIView *rightH = [_rightHandleView hitTest:[self convertPoint:point toView:_rightHandleView] withEvent:event];
        if (leftH) {
            return leftH;
        } else if (rightH) {
            return rightH;
        } else {
            return [super hitTest:point withEvent:event];
        }
    }
    return [super hitTest:point withEvent:event];
}

- (BOOL)pointInside:(CGPoint)point withEvent:(UIEvent *)event {
    CGRect hitFrame = CGRectInset(self.bounds, -10, -20);
    if (CGRectContainsPoint(hitFrame, point)) {
        BOOL result = [_leftHandleView pointInside:[self convertPoint:point toView:_leftHandleView] withEvent:event]
        || [_rightHandleView pointInside:[self convertPoint:point toView:_rightHandleView] withEvent:event];
        if (!result) {
            return [super pointInside:point withEvent:event];
        } else {
            return result;
        }
    }
    return [super pointInside:point withEvent:event];
}

// MARK: - Scroll View Delegate

- (void)scrollViewDidEndDecelerating:(UIScrollView *)scrollView {
    [self updateSelectedTime:YES];
}

- (void)scrollViewDidEndDragging:(UIScrollView *)scrollView willDecelerate:(BOOL)decelerate {
    if (!decelerate) {
        [self seekToTime:[self startTime]];
        [self updateSelectedTime:YES];
    }
}

- (void)scrollViewWillBeginDragging:(UIScrollView *)scrollView {
    [self seekToTime:[self startTime]];
    _beginDragging = YES;
}

- (void)scrollViewDidScroll:(UIScrollView *)scrollView {
    if (_beginDragging) {
        [self updateSelectedTime:NO];
    }
}

- (void)dealloc {
    DLog(@"%@ 销毁了.", NSStringFromClass(self.class));
}

@end
