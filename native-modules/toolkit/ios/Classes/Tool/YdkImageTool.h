//
//  YdkImageTool.h
//  ydk-toolkit
//
//  Created by yryz on 2019/7/4.
//

#import <Foundation/Foundation.h>

FOUNDATION_EXPORT NSErrorDomain const YdkImageToolErrorDomain;
NS_ERROR_ENUM(YdkImageToolErrorDomain)
{
    YdkImageToolErrorSizeZero                 = -1001,    // 图片大小有误
};

@interface YdkImageTool : NSObject

/**
 保存图片到指定本地指定路径下
 
 @param imageURL 图片url
 @param targetURL 指定路径
 @return 返回保存结果信号
 */
+ (BOOL)saveImageURL:(NSURL *)imageURL targetURL:(NSURL *)targetURL;

/**
 保存图片到指定本地指定路径下
 
 @param image UIImage or NSData type
 @param targetURL 指定路径
 @return 返回保存结果
 */
+ (BOOL)saveImage:(id)image targetURL:(NSURL *)targetURL;

@end
