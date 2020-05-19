//
//  UIImage+toolkit.h
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import <UIKit/UIKit.h>

#define KB256 (1024 * 256)
#define KB512 KB256 * 2
#define M1 KB512 * 2

@interface UIImage (toolkit)

+ (UIImage *)tk_imageWithIcon:(NSString *)iconCode size:(NSUInteger)size color:(UIColor *)color;
+ (UIImage *)tk_imageWithIcon:(NSString *)iconCode inFont:(NSString *)fontName size:(NSUInteger)size color:(UIColor *)color;
/**
 ICON 字体图标

 @param iconCode code
 @param fontName 字体名称
 @param size 大小pt，如果是长宽不一致时，按照短的一边传size
 @param color 颜色
 @param backgroundColor 背景色
 @return UIImage
 */
+ (UIImage *)tk_imageWithIcon:(NSString *)iconCode inFont:(NSString *)fontName size:(NSUInteger)size color:(UIColor *)color backgroundColor:(UIColor *)backgroundColor;

/**
 压缩图片
 
 @param maxLength 控制图片大小(eg. 1M = 1024 * 1024)
 @return 图片NSData
 */
- (NSData *)tk_compressImageWithMaxLength:(NSUInteger)maxLength;

/**
 图片裁剪
 
 @param size 图片尺寸
 @return UIImage
 */
- (UIImage *)tk_cropImageWithSize:(CGSize)size;

@end
