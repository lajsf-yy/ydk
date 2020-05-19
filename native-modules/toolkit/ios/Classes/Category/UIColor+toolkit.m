//
//  UIColor+toolkit.m
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import "UIColor+toolkit.h"

@implementation UIColor (toolkit)

+ (UIColor *)tk_randomColor {
    return [UIColor colorWithRed:(CGFloat)random() / (CGFloat)RAND_MAX
                           green:(CGFloat)random() / (CGFloat)RAND_MAX
                            blue:(CGFloat)random() / (CGFloat)RAND_MAX
                           alpha:1.0f];
}

static inline NSUInteger tk_hexStrToInt(NSString *str) {
    uint32_t result = 0;
    sscanf([str UTF8String], "%X", &result);
    return result;
}

static BOOL tk_hexStrToRGBA(NSString *str,
                         CGFloat *r, CGFloat *g, CGFloat *b, CGFloat *a) {
    NSCharacterSet *set = [NSCharacterSet whitespaceAndNewlineCharacterSet];
    str = [[str stringByTrimmingCharactersInSet:set] uppercaseString];
    if ([str hasPrefix:@"#"]) {
        str = [str substringFromIndex:1];
    } else if ([str hasPrefix:@"0X"]) {
        str = [str substringFromIndex:2];
    }
    
    NSUInteger length = [str length];
    //         RGB            RGBA          RRGGBB        RRGGBBAA
    if (length != 3 && length != 4 && length != 6 && length != 8) {
        return NO;
    }
    
    //RGB,RGBA,RRGGBB,RRGGBBAA
    if (length < 5) {
        *r = tk_hexStrToInt([str substringWithRange:NSMakeRange(0, 1)]) / 255.0f;
        *g = tk_hexStrToInt([str substringWithRange:NSMakeRange(1, 1)]) / 255.0f;
        *b = tk_hexStrToInt([str substringWithRange:NSMakeRange(2, 1)]) / 255.0f;
        if (length == 4)  *a = tk_hexStrToInt([str substringWithRange:NSMakeRange(3, 1)]) / 255.0f;
        else *a = 1;
    } else {
        *r = tk_hexStrToInt([str substringWithRange:NSMakeRange(0, 2)]) / 255.0f;
        *g = tk_hexStrToInt([str substringWithRange:NSMakeRange(2, 2)]) / 255.0f;
        *b = tk_hexStrToInt([str substringWithRange:NSMakeRange(4, 2)]) / 255.0f;
        if (length == 8) *a = tk_hexStrToInt([str substringWithRange:NSMakeRange(6, 2)]) / 255.0f;
        else *a = 1;
    }
    return YES;
}

+ (instancetype)tk_colorWithHexString:(NSString *)hexStr {
    CGFloat r, g, b, a;
    if (tk_hexStrToRGBA(hexStr, &r, &g, &b, &a)) {
        return [UIColor colorWithRed:r green:g blue:b alpha:a];
    }
    return nil;
}

+ (UIColor *)tk_colorWithRGB:(uint32_t)rgbValue {
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16) / 255.0f
                           green:((rgbValue & 0xFF00) >> 8) / 255.0f
                            blue:(rgbValue & 0xFF) / 255.0f
                           alpha:1];
}

+ (UIColor *)tk_colorWithRGBA:(uint32_t)rgbaValue {
    return [UIColor colorWithRed:((rgbaValue & 0xFF000000) >> 24) / 255.0f
                           green:((rgbaValue & 0xFF0000) >> 16) / 255.0f
                            blue:((rgbaValue & 0xFF00) >> 8) / 255.0f
                           alpha:(rgbaValue & 0xFF) / 255.0f];
}

+ (UIColor *)tk_colorWithRGB:(uint32_t)rgbValue alpha:(CGFloat)alpha {
    return [UIColor colorWithRed:((rgbValue & 0xFF0000) >> 16) / 255.0f
                           green:((rgbValue & 0xFF00) >> 8) / 255.0f
                            blue:(rgbValue & 0xFF) / 255.0f
                           alpha:alpha];
}

@end
