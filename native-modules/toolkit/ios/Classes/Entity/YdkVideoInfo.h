//
//  YdkVideoInfo.h
//  ydk-toolkit
//
//  Created by yryz on 2019/7/4.
//

#import <Foundation/Foundation.h>

@interface YdkVideoInfo : NSObject

@property (nonatomic, copy) NSString *filePath;
@property (nonatomic, copy) NSString *fileName;
@property (nonatomic, copy) NSString *thumbnailPath;
@property (nonatomic, assign) int duration; // second
@property (nonatomic, assign) long long size; // KB

+ (YdkVideoInfo *)videoInfoWithVideoURL:(NSURL *)videoURL;
+ (YdkVideoInfo *)videoInfoWithVideoURL:(NSURL *)videoURL thumbnailURL:(NSURL *)thumbnailURL;

@end
