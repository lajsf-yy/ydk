//
//  YdkVideoTool.m
//  ydk-toolkit
//
//  Created by yryz on 2019/7/4.
//

#import "YdkVideoTool.h"
#import <AVFoundation/AVFoundation.h>

@implementation YdkVideoTool

+ (UIImage *)thumbnailImageFromVideoURL:(NSURL *)videoURL {
    return [self thumbnailImageFromVideoURL:videoURL atTime:0];
}

+ (UIImage *)thumbnailImageFromVideoURL:(NSURL *)videoURL atTime:(double)second {
    return [self thumbnailImageFromVideoURL:videoURL atTime:second maximunSize:[UIScreen mainScreen].bounds.size];
}

+ (UIImage *)thumbnailImageFromVideoURL:(NSURL *)videoURL atTime:(double)second maximunSize:(CGSize)size {
    if (!videoURL || !videoURL.isFileURL) {
        return nil;
    }
    AVAsset *asset = [AVAsset assetWithURL:videoURL];
    return [self thumbnailImageFromAsset:asset atTime:second maximunSize:size];
}

+ (UIImage *)thumbnailImageFromAsset:(AVAsset *)asset atTime:(double)second maximunSize:(CGSize)size {
    if (!asset) {
        return nil;
    }
    
    // 获取视频图像实际开始时间 部分视频并非一开始就是有图像的 因此要获取视频的实际开始片段
    AVAssetTrack *track = [asset tracksWithMediaType:AVMediaTypeVideo].firstObject;
    NSArray<AVAssetTrackSegment *> *segments = track.segments;
    if (!segments.count) {
        return nil;
    }
    CMTime startTime = kCMTimeZero; // 视频实际开始时间
    for (AVAssetTrackSegment *segment in segments) {
        if (!segment.isEmpty) {
            startTime = segment.timeMapping.target.start;
            break;
        }
    }
    
    CMTime frameTime = CMTimeMakeWithSeconds(second, asset.duration.timescale);
    // 指定时间不在视频有效时间范围内，则返回实际开始时间
    if (CMTimeCompare(frameTime, asset.duration) == 1 || CMTimeCompare(frameTime, startTime) == -1) {
        frameTime = startTime;
    }
    AVAssetImageGenerator *imageGenerator = [AVAssetImageGenerator assetImageGeneratorWithAsset:asset];
    imageGenerator.requestedTimeToleranceBefore = kCMTimeZero;
    imageGenerator.requestedTimeToleranceAfter = kCMTimeZero;
    imageGenerator.appliesPreferredTrackTransform = YES;
    imageGenerator.maximumSize = size;
    
    NSError *error = nil;
    CMTime actualTime;
    CGImageRef imageRef = [imageGenerator copyCGImageAtTime:frameTime actualTime:&actualTime error:&error];
    if (error || imageRef == NULL) {
        return nil;
    }
    UIImage *thumbnailImage = [UIImage imageWithCGImage:imageRef];
    CGImageRelease(imageRef);
    return thumbnailImage;
}

@end
