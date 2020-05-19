//
//  NSData+toolkit.h
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import <Foundation/Foundation.h>

@interface NSData (toolkit)

// 获取图片NSData后缀，除gif之外，所有图片格式都用jpg
- (NSString *)tk_imageFileSuffix;

@end
