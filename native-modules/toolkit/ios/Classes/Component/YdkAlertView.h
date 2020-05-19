//
//  YdkAlertViewController.h
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, YdkAlertActionStyle) {
    YdkAlertActionStyleDefault = 0,
    YdkAlertActionStyleCancel,
    YdkAlertActionStyleDestructive
};

typedef NS_ENUM(NSInteger, YdkAlertStyle) {
    YdkAlertStyleAlert = 0,
    YdkAlertStyleActionSheet,
};

@interface YdkAlertAction : NSObject

+ (instancetype)actionWithTitle:(NSString *)title style:(YdkAlertActionStyle)style handler:(void (^)(void))handler;

@property (nonatomic, getter=isEnabled) BOOL enabled;

@end

@interface YdkAlertView : UIView

// 针对YdkAlertStyleAlert点击背景时的回调，不做任何事情，需要自己调用dismiss
@property (nonatomic, copy) void(^touchBackgroundAction)(void);

@property (readonly, nonatomic, strong) UIView *contentView;
@property (readonly, nonatomic, strong) UIView *defaultContainer;
@property (readonly, nonatomic, strong) UIView *backgroundView;

@property (readonly, nonatomic, assign, getter=isVisible) BOOL visible;

+ (instancetype)alertWithTitle:(NSString *)title message:(NSString *)message;
+ (instancetype)alertWithTitle:(NSString *)title message:(NSString *)message preferredStyle:(YdkAlertStyle)preferredStyle;

- (void)addAction:(YdkAlertAction *)action;

- (instancetype)initWithTitle:(NSString *)title message:(NSString *)message preferredStyle:(YdkAlertStyle)preferredStyle;
- (instancetype)initWithContentView:(UIView *)contentView preferredStyle:(YdkAlertStyle)preferredStyle;

- (void)show;

- (void)dismiss;

@end
