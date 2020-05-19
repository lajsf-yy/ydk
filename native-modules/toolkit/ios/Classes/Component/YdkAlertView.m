//
//  YdkAlertViewController.m
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import "YdkAlertView.h"
#import "UIColor+toolkit.h"
#import "UIView+toolkit.h"
#import "YdkToolkitMacros.h"

#define YdkActionDefaultBackgroundColor UIColorHex(ff3988)

// MARK: - YdkAlertAction
@interface YdkAlertAction ()

@property (nonatomic, copy) NSString *title;
@property (nonatomic, assign) YdkAlertActionStyle style;
@property (nonatomic, copy) void(^handler)(void);

@end

@implementation YdkAlertAction

+ (instancetype)actionWithTitle:(NSString *)title style:(YdkAlertActionStyle)style handler:(void (^)(void))handler {
    YdkAlertAction *action = [[self alloc] init];
    action.title = title;
    action.style = style;
    action.handler = handler;
    return action;
}

@end

@interface YdkAlertView ()

@property (nonatomic, copy) NSString *headline;
@property (nonatomic, copy) NSString *message;
@property (nonatomic, assign) YdkAlertStyle preferredStyle;

//@property (nonatomic, strong) NSMutableArray<LLAlertAction *> *actions;

@property (nonatomic, strong) YdkAlertAction *confirmAction;
@property (nonatomic, strong) YdkAlertAction *cancelAction;
@property (nonatomic, strong) YdkAlertAction *destructiveAction;

// UI
@property (nonatomic, strong) UIView *customContainerV;

@property (nonatomic, strong) UIView *containerV;
@property (nonatomic, strong) UILabel *titleL;
@property (nonatomic, strong) UILabel *messageL;
@property (nonatomic, strong) UIButton *confirmB;
@property (nonatomic, strong) UIButton *cancelB;
@property (readwrite) UIView *defaultContainer;

@property (nonatomic, strong) UIControl *backgroundC;

@end

@implementation YdkAlertView

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        [self setupUI];
        [self layoutUI];
    }
    return self;
}

- (instancetype)initWithContentView:(UIView *)contentView preferredStyle:(YdkAlertStyle)preferredStyle {
    _customContainerV = contentView;
    _preferredStyle = preferredStyle;
    return [self initWithFrame:[UIScreen mainScreen].bounds];
}

- (instancetype)initWithTitle:(NSString *)title message:(NSString *)message preferredStyle:(YdkAlertStyle)preferredStyle{
    _headline = [title copy];
    _message = [message copy];
    _preferredStyle = preferredStyle;
    return [self initWithFrame:[UIScreen mainScreen].bounds];
}

+ (instancetype)alertWithTitle:(NSString *)title message:(NSString *)message {
    return [self alertWithTitle:title message:message preferredStyle:YdkAlertStyleAlert];
}

+ (instancetype)alertWithTitle:(NSString *)title message:(NSString *)message preferredStyle:(YdkAlertStyle)preferredStyle {
    YdkAlertView *alert = [[self alloc] initWithTitle:title message:message preferredStyle:preferredStyle];
    return alert;
}

- (UIView *)contentView {
    return _customContainerV ? : _containerV;
}

- (UIView *)backgroundView {
    return _backgroundC;
}

- (void)setupUI {
    // 背景遮罩
    UIControl *backgroundC = [[UIControl alloc] initWithFrame:self.bounds];
    backgroundC.backgroundColor = [UIColor colorWithWhite:0.0 alpha:0.6];
    backgroundC.alpha = 0.0;
    [backgroundC addTarget:self action:@selector(didSelectBackground) forControlEvents:UIControlEventTouchUpInside];
    [self addSubview:_backgroundC = backgroundC];
    
    if (_customContainerV) {
        [self addSubview:_customContainerV];
        return;
    }
    
    UIView *containerV = [[UIView alloc] init];
    containerV.backgroundColor = UIColorHex(ffffff);
    containerV.layer.cornerRadius = TK_TransformPT_W(9);
    containerV.layer.masksToBounds = YES;
    [self addSubview:_containerV = containerV];
    
    if (_headline && _headline.length > 0) {
        UILabel *titleL = [[UILabel alloc] init];
        titleL.font = [UIFont boldSystemFontOfSize:TK_TransformPT_W(20)];
        titleL.textColor = UIColorHex(000000);
        titleL.text = _headline;
        [titleL sizeToFit];
        [containerV addSubview:_titleL = titleL];
    }
    
    if (_message && _message.length > 0) {
        BOOL haveHeaded = _headline && _headline.length > 0;
        NSMutableParagraphStyle *ps = [[NSMutableParagraphStyle alloc] init];
        ps.lineSpacing = haveHeaded ? TK_TransformPT_W(4) : TK_TransformPT_W(6);
        NSDictionary *attributes = @{ NSFontAttributeName : [UIFont systemFontOfSize:(haveHeaded ? TK_TransformPT_W(14) : TK_TransformPT_W(15))],
                                      NSParagraphStyleAttributeName : ps,
                                      NSForegroundColorAttributeName : haveHeaded ? UIColorHex(333333) : UIColorHex(000000) };
        UILabel *messageL = [[UILabel alloc] init];
        messageL.numberOfLines = 0;
        messageL.attributedText = [[NSAttributedString alloc] initWithString:_message attributes:attributes];
        [containerV addSubview:_messageL = messageL];
    }
    
    UIView *defaultContainer = [[UIView alloc] init];
    defaultContainer.size = CGSizeMake(TK_TransformPT_W(120), TK_TransformPT_W(36));
    defaultContainer.backgroundColor = YdkActionDefaultBackgroundColor;
    [defaultContainer tk_setCornerRadius:TK_TransformPT_W(18)];
    [containerV addSubview:_defaultContainer = defaultContainer];
    
    UIButton *confirmB = [UIButton buttonWithType:UIButtonTypeCustom];
    confirmB.size = defaultContainer.size;
    confirmB.titleLabel.font = [UIFont systemFontOfSize:TK_TransformPT_W(13)];
    [confirmB setTitle:@"好的" forState:UIControlStateNormal];
    [confirmB setTitleColor:UIColorHex(ffffff) forState:UIControlStateNormal];
    [confirmB addTarget:self action:@selector(confirmAction:) forControlEvents:UIControlEventTouchUpInside];
    [defaultContainer addSubview:_confirmB = confirmB];
    
    UIButton *cancelB = [UIButton buttonWithType:UIButtonTypeCustom];
    cancelB.titleLabel.font = [UIFont systemFontOfSize:TK_TransformPT_W(13)];
    cancelB.size = CGSizeMake(TK_TransformPT_W(120), TK_TransformPT_W(36));
    cancelB.backgroundColor = UIColorHex(ffffff);
    cancelB.layer.borderColor = UIColorHex(b1b1b1).CGColor;
    cancelB.layer.borderWidth = 1 / [UIScreen mainScreen].scale;
    cancelB.layer.masksToBounds = YES;
    cancelB.layer.cornerRadius = TK_TransformPT_W(18);
    [cancelB setTitle:@"取消" forState:UIControlStateNormal];
    [cancelB setTitleColor:UIColorHex(000000) forState:UIControlStateNormal];
    cancelB.hidden = YES;
    [cancelB addTarget:self action:@selector(cancelAction:) forControlEvents:UIControlEventTouchUpInside];
    [containerV addSubview:_cancelB = cancelB];
}

- (void)confirmAction:(id)sender {
    if (self.confirmAction && self.confirmAction.handler) {
        self.confirmAction.handler();
    }
    [self dismiss];
}

- (void)cancelAction:(id)sender {
    if (self.cancelAction && self.cancelAction.handler) {
        self.cancelAction.handler();
    }
    [self dismiss];
}

// MARK: - Public Method
- (void)addAction:(YdkAlertAction *)action {
    switch (action.style) {
        case YdkAlertActionStyleDefault:
            _confirmAction = action;
            break;
        case YdkAlertActionStyleCancel:
            _cancelAction = action;
            break;
        case YdkAlertActionStyleDestructive:
            _destructiveAction = action;
            break;
    }
    [self layoutUI];
}

// MARK: - Layout
- (void)layoutUI {
    _backgroundC.frame = self.bounds;
    if (_customContainerV) {
        _customContainerV.center = [self centerForContainerView:_customContainerV];
        return;
    }
    
    CGFloat width = self.width - TK_TransformPT_W(36 * 2);
    CGFloat height = TK_TransformPT_W(34);
    if (_titleL) {
        _titleL.top = TK_TransformPT_W(34);
        height = _titleL.bottom + TK_TransformPT_W(16);
        _titleL.centerX = width / 2.0;
    }
    if (_messageL) {
        _messageL.top = height;
        CGSize size = [_messageL sizeThatFits:CGSizeMake(width - TK_TransformPT_W(36 * 2), MAXFLOAT)];
        _messageL.size = size;
        _messageL.centerX = width / 2.0;
        height = _messageL.bottom + TK_TransformPT_W(30);
        _messageL.textAlignment = size.height > TK_TransformPT_W(50) ? NSTextAlignmentLeft : NSTextAlignmentCenter;
    }
    
    _defaultContainer.top = height;
    _defaultContainer.centerX = width / 2.0;
    [_confirmB setTitle:(_confirmAction.title ? : @"好的") forState:UIControlStateNormal];
    
    height = _defaultContainer.bottom + TK_TransformPT_W(32);
    if (_cancelAction) {
        _cancelB.top = _defaultContainer.top;
        _cancelB.right = width / 2.0 - TK_TransformPT_W(7.5);
        _defaultContainer.left = width / 2.0 + TK_TransformPT_W(7.5);
        _cancelB.hidden = NO;
        [_cancelB setTitle:_cancelAction.title ? : @"取消" forState:UIControlStateNormal];
    }
    _containerV.size = CGSizeMake(width, height);
    _containerV.center = [self centerForContainerView:_containerV];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    
    [_confirmB setTitle:(_confirmAction.title ? : @"好的") forState:UIControlStateNormal];
    if (_cancelAction) {
        [_cancelB setTitle:_cancelAction.title ? : @"取消" forState:UIControlStateNormal];
    }
}

- (CGPoint)centerForContainerView:(UIView *)containerV {
    if (_preferredStyle == YdkAlertStyleAlert) {
        return CGPointMake(self.width / 2.0, self.height / 2.0);
    } else {
        return CGPointMake(self.width / 2.0, self.height - containerV.height / 2.0);
    }
}

// MARK: - Action
- (void)didSelectBackground {
    if (_preferredStyle == YdkAlertStyleActionSheet) {
        [self cancelAction:nil];
    }
    if (_touchBackgroundAction) {
        _touchBackgroundAction();
    }
}

// MARK: - Public Method
#define TransitionDuration .5f
- (void)show {
    _visible = YES;
    if (!self.superview) {
        [[UIApplication sharedApplication].keyWindow addSubview:self];
    }
    CGAffineTransform transform;
    if (_preferredStyle == YdkAlertStyleAlert) {
        transform = CGAffineTransformMakeScale(0.4, 0.4);
    } else {
        transform = CGAffineTransformMakeTranslation(0, self.contentView.height);
    }
    self.contentView.transform = transform;
    [UIView animateWithDuration:TransitionDuration delay:0.0 usingSpringWithDamping:0.6 initialSpringVelocity:0.6 options:0 animations:^{
        self.contentView.transform = CGAffineTransformIdentity;
        self.backgroundC.alpha = 1.0;
    } completion:^(BOOL finished) {
        
    }];
}

- (void)dismiss {
    if (!_visible) return;
    
    _visible = NO;
    CGAffineTransform transform;
    if (_preferredStyle == YdkAlertStyleAlert) {
        transform = CGAffineTransformMakeScale(0.4, 0.4);
    } else {
        transform = CGAffineTransformMakeTranslation(0, self.contentView.height);
    }
    
    [UIView animateWithDuration:TransitionDuration delay:0.0 usingSpringWithDamping:0.6 initialSpringVelocity:0.6 options:0 animations:^{
        self.contentView.transform = transform;
        self.backgroundC.alpha = 0.0;
        self.contentView.alpha = 0.0;
    } completion:^(BOOL finished) {
        // 针对传过来的内容视图恢复alpha
        self.customContainerV.alpha = 1.0;
        [self removeFromSuperview];
    }];
}

@end
