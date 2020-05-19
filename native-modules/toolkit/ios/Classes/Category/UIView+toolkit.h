//
//  UIView+toolkit.h
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import <UIKit/UIKit.h>

@interface UIView (toolkit)

// 只针对固定/已确定size的视图有效
- (void)tk_setCornerRadius:(CGFloat)cornerRadius;

- (void)tk_setCornerRadius:(CGFloat)radius corner:(UIRectCorner)corner;

@property (nonatomic) CGFloat left;        ///< Shortcut for frame.origin.x.
@property (nonatomic) CGFloat top;         ///< Shortcut for frame.origin.y
@property (nonatomic) CGFloat right;       ///< Shortcut for frame.origin.x + frame.size.width
@property (nonatomic) CGFloat bottom;      ///< Shortcut for frame.origin.y + frame.size.height
@property (nonatomic) CGFloat width;       ///< Shortcut for frame.size.width.
@property (nonatomic) CGFloat height;      ///< Shortcut for frame.size.height.
@property (nonatomic) CGFloat centerX;     ///< Shortcut for center.x
@property (nonatomic) CGFloat centerY;     ///< Shortcut for center.y
@property (nonatomic) CGPoint origin;      ///< Shortcut for frame.origin.
@property (nonatomic) CGSize  size;        ///< Shortcut for frame.size.

@property (nullable, nonatomic, readonly) UIViewController *viewController;

@end
