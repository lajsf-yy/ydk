//
//  YdkHandlerView.m
//  ydk-trimmer
//
//  Created by yryz on 2019/9/4.
//

#import "YdkHandlerView.h"

static const CGFloat kHandleKnobWidth = 1;
static const CGFloat kHandleKnobMargin = 1;

@interface YdkHandlerView ()

@property (nonatomic, strong) UIView *knobView1;
@property (nonatomic, strong) UIView *knobView2;

@end

@implementation YdkHandlerView

- (void)setKnobColor:(UIColor *)knobColor {
    _knobColor = knobColor;
    _knobView1.backgroundColor = knobColor;
    _knobView2.backgroundColor = knobColor;
}

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        _knobColor = [UIColor lightGrayColor];
        [self setupSubviews];
    }
    return self;
}

- (void)setupSubviews {
    UIView *knobView1 = [[UIView alloc] init];
    knobView1.translatesAutoresizingMaskIntoConstraints = NO;
    [self addSubview:_knobView1 = knobView1];
    
    [self addConstraint:[NSLayoutConstraint constraintWithItem:knobView1 attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1.0/6 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:knobView1 attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:0 constant:kHandleKnobWidth]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:knobView1 attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterX multiplier:1 constant:-(kHandleKnobWidth + kHandleKnobMargin) / 2.0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:knobView1 attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterY multiplier:1 constant:0]];
    
    UIView *knobView2 = [[UIView alloc] init];
    knobView2.translatesAutoresizingMaskIntoConstraints = NO;
    [self addSubview:_knobView2 = knobView2];
    
    [self addConstraint:[NSLayoutConstraint constraintWithItem:knobView2 attribute:NSLayoutAttributeHeight relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeHeight multiplier:1.0/6 constant:0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:knobView2 attribute:NSLayoutAttributeWidth relatedBy:NSLayoutRelationEqual toItem:nil attribute:NSLayoutAttributeNotAnAttribute multiplier:0 constant:kHandleKnobWidth]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:knobView2 attribute:NSLayoutAttributeCenterX relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterX multiplier:1 constant:(kHandleKnobWidth + kHandleKnobMargin) / 2.0]];
    [self addConstraint:[NSLayoutConstraint constraintWithItem:knobView2 attribute:NSLayoutAttributeCenterY relatedBy:NSLayoutRelationEqual toItem:self attribute:NSLayoutAttributeCenterY multiplier:1 constant:0]];
}

- (UIView *)hitTest:(CGPoint)point withEvent:(UIEvent *)event {
    CGRect hitFrame = CGRectInset(self.bounds, -10, -20);
    return CGRectContainsPoint(hitFrame, point) ? self : nil;
}

- (BOOL)pointInside:(CGPoint)point withEvent:(UIEvent *)event {
    CGRect hitFrame = CGRectInset(self.bounds, -10, -20);
    return CGRectContainsPoint(hitFrame, point);
}

@end
