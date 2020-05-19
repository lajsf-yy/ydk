//
//  UIImage+toolkit.m
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import "UIImage+toolkit.h"
#import "UILabel+toolkit.h"

@implementation UIImage (toolkit)

+ (UIImage *)tk_imageWithIcon:(NSString *)iconCode size:(NSUInteger)size color:(UIColor *)color {
    return [self tk_imageWithIcon:iconCode inFont:@"iconfont" size:size color:color];
}

+ (UIImage *)tk_imageWithIcon:(NSString *)iconCode inFont:(NSString *)fontName size:(NSUInteger)size color:(UIColor *)color {
    return [self tk_imageWithIcon:iconCode inFont:fontName size:size color:color backgroundColor:[UIColor clearColor]];
}

+ (UIImage *)tk_imageWithIcon:(NSString *)iconCode inFont:(NSString *)fontName size:(NSUInteger)size color:(UIColor *)color backgroundColor:(UIColor *)backgroundColor {
    UILabel *label = [UILabel tk_labelWithIcon:iconCode inFont:fontName size:size color:color backgroundColor:backgroundColor];
    CGSize imageSize = CGSizeMake(label.frame.size.width, label.frame.size.height);
    UIGraphicsBeginImageContextWithOptions(imageSize, NO, [[UIScreen mainScreen] scale]);
    [label.layer renderInContext:UIGraphicsGetCurrentContext()];
    UIImage *retImage = UIGraphicsGetImageFromCurrentImageContext();
    return retImage;
}

- (NSData *)tk_compressImageWithMaxLength:(NSUInteger)maxLength {
    CGFloat compression = 1;
    NSData *data = UIImageJPEGRepresentation(self, compression);
    if (data.length < maxLength) return data;
    
    CGFloat max = 1;
    CGFloat min = 0;
    // STEP1: 压缩图片质量。二分法，循环次数为0~1二分算法次数6次
    for (int i = 0; i < 6; ++i) {
        compression = (max + min) / 2;
        data = UIImageJPEGRepresentation(self, compression);
        if (data.length < maxLength * 0.9) {
            min = compression;
        } else if (data.length > maxLength) {
            max = compression;
        } else {
            break;
        }
    }
    if (data.length < maxLength) return data;
    
    UIImage *resultImage = [UIImage imageWithData:data];
    NSUInteger lastDataLength = 0;
    // STEP2: 压缩图片尺寸。
    while (data.length > maxLength && data.length != lastDataLength) {
        lastDataLength = data.length;
        CGFloat ratio = (CGFloat)maxLength / data.length;
        CGSize size = CGSizeMake((NSUInteger)(resultImage.size.width * sqrtf(ratio)),
                                 (NSUInteger)(resultImage.size.height * sqrtf(ratio)));
        resultImage = [resultImage tk_cropImageWithSize:size];
        data = UIImageJPEGRepresentation(resultImage, compression);
    }
    return data;
}

- (UIImage *)tk_cropImageWithSize:(CGSize)size {
    UIImage *resultImage = [self copy];
    UIGraphicsBeginImageContext(size);
    [resultImage drawInRect:CGRectMake(0, 0, size.width, size.height)];
    resultImage = UIGraphicsGetImageFromCurrentImageContext();
    UIGraphicsEndImageContext();
    return resultImage;
}

@end
