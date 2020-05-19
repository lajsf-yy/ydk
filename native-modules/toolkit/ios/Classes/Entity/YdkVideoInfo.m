//
//  YdkVideoInfo.m
//  ydk-toolkit
//
//  Created by yryz on 2019/7/4.
//

#import "YdkVideoInfo.h"
#import "YdkVideoTool.h"
#import "YdkFileSystemTool.h"
#import "YdkImageTool.h"

#import <AVFoundation/AVFoundation.h>

@implementation YdkVideoInfo

+ (YdkVideoInfo *)videoInfoWithVideoURL:(NSURL *)videoURL {
    return [self videoInfoWithVideoURL:videoURL thumbnailURL:nil];
}
+ (YdkVideoInfo *)videoInfoWithVideoURL:(NSURL *)videoURL thumbnailURL:(NSURL *)thumbnailURL {
    YdkVideoInfo *videoInfo = [[YdkVideoInfo alloc] init];
    NSString *videoPath = videoURL.path;
    NSString *fileName = [NSString stringWithFormat:@"%@.jpg", [[videoPath lastPathComponent] stringByDeletingPathExtension]];
    if (!thumbnailURL) {
        thumbnailURL = [YdkFileSystemTool createCacheFileURLWithComponent:@"video" fileName:fileName];
    }
    NSString *thumbnailPath = thumbnailURL.path;
    UIImage *videoImage = [YdkVideoTool thumbnailImageFromVideoURL:videoURL];
    BOOL result = [YdkImageTool saveImage:videoImage targetURL:[NSURL fileURLWithPath:thumbnailPath]];
    if (!result) {
        return nil;
    }
    
    videoInfo.thumbnailPath = thumbnailPath;
    videoInfo.filePath = [videoPath copy];
    videoInfo.fileName = [[videoPath lastPathComponent] stringByDeletingPathExtension]/*videoPath.lastPathComponent*/;
    AVAsset *asset = [AVAsset assetWithURL:videoURL];
    CMTime time = [asset duration];
    videoInfo.duration = round((time.value * 1.0f) / (time.timescale * 1.0));
    videoInfo.size = [[NSFileManager defaultManager] attributesOfItemAtPath:videoPath error:nil].fileSize / 1024;
    return videoInfo;
}

@end
