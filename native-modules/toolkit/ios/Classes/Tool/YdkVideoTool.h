//
//  YdkVideoTool.h
//  ydk-toolkit
//
//  Created by yryz on 2019/7/4.
//

#import <Foundation/Foundation.h>

@class AVAsset;
@interface YdkVideoTool : NSObject

+ (UIImage *)thumbnailImageFromVideoURL:(NSURL *)videoURL;
+ (UIImage *)thumbnailImageFromVideoURL:(NSURL *)videoURL atTime:(double)second;

/**
 根据视频url获取视频缩略图
 
 @param videoURL 视频url
 @param second 某一时间，如果不在有效范围内，则返回实际开始时间的视频缩略图像
 @param size 图片size
 @return 视频缩略图UIImage
 */
+ (UIImage *)thumbnailImageFromVideoURL:(NSURL *)videoURL atTime:(double)second maximunSize:(CGSize)size;
+ (UIImage *)thumbnailImageFromAsset:(AVAsset *)asset atTime:(double)second maximunSize:(CGSize)size;

@end
