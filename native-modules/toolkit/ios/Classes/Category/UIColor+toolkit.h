//
//  UIColor+toolkit.h
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#ifndef UIColorHex
#define UIColorHex(_hex_)   [UIColor tk_colorWithHexString:((__bridge NSString *)CFSTR(#_hex_))]
#endif

#import <UIKit/UIKit.h>

@interface UIColor (toolkit)

+ (UIColor *)tk_randomColor;

+ (UIColor *)tk_colorWithRGB:(uint32_t)rgbValue;

+ (UIColor *)tk_colorWithRGBA:(uint32_t)rgbaValue;

+ (UIColor *)tk_colorWithRGB:(uint32_t)rgbValue alpha:(CGFloat)alpha;

+ (UIColor *)tk_colorWithHexString:(NSString *)hexStr;

@end
