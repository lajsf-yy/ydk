//
//  UILabel+toolkit.m
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import "UILabel+toolkit.h"

@implementation UILabel (toolkit)

// 如果是长宽不一致时，按照短的一边传size
+ (UILabel *)tk_labelWithIcon:(NSString *)iconCode size:(NSUInteger)size color:(UIColor *)color {
    return [self tk_labelWithIcon:iconCode inFont:@"iconfont" size:size color:color];
}

+ (UILabel *)tk_labelWithIcon:(NSString *)iconCode inFont:(NSString *)fontName size:(NSUInteger)size color:(UIColor *)color {
    return [self tk_labelWithIcon:iconCode inFont:fontName size:size color:color backgroundColor:[UIColor clearColor]];
}

+ (UILabel *)tk_labelWithIcon:(NSString *)iconCode inFont:(NSString *)fontName size:(NSUInteger)size color:(UIColor *)color backgroundColor:(UIColor *)backgroundColor {
    UILabel *label = [[UILabel alloc] initWithFrame:CGRectMake(0, 0, size, size)];
    label.font = [UIFont fontWithName:fontName size:size];
    label.text = iconCode;
    label.backgroundColor = backgroundColor;
    if (color) {
        label.textColor = color;
    }
    [label sizeToFit];
    return label;
}

@end
