//
//  UILabel+toolkit.h
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import <UIKit/UIKit.h>

@interface UILabel (toolkit)

// 如果是长宽不一致时，按照短的一边传size
+ (UILabel *)tk_labelWithIcon:(NSString *)iconCode size:(NSUInteger)size color:(UIColor *)color;
+ (UILabel *)tk_labelWithIcon:(NSString *)iconCode inFont:(NSString *)fontName size:(NSUInteger)size color:(UIColor *)color;
+ (UILabel *)tk_labelWithIcon:(NSString *)iconCode inFont:(NSString *)fontName size:(NSUInteger)size color:(UIColor *)color backgroundColor:(UIColor *)backgroundColor;

@end

