//
//  YdkFileSystemTool.h
//  ydk-toolkit
//
//  Created by yryz on 2019/7/4.
//

#import <Foundation/Foundation.h>

@interface YdkFileSystemTool : NSObject

+ (NSString *)documentDirectoryForComponent:(NSString *)component;
+ (NSString *)cachesDirectoryForComponent:(NSString *)component;
+ (NSString *)tempDirectoryForComponent:(NSString *)component;

+ (NSURL *)createCacheFileURLWithComponent:(NSString *)component;
+ (NSURL *)createCacheFileURLWithComponent:(NSString *)component fileName:(NSString *)fileName;

+ (NSURL *)createTempFileURLWithComponent:(NSString *)component;
+ (NSURL *)createTempFileURLWithComponent:(NSString *)component fileName:(NSString *)fileName;

// 获取缓存大小[在cache目录下]，单位Byte
+ (unsigned long long)cacheSize;

// 获取指定目录及子目录总大小
+ (unsigned long long)fileSizeFromDirectory:(NSString *)directory;

// 清除缓存[在cache目录下]
+ (void)cleanCache;
+ (void)cleanCacheWithCompletionBlock:(void(^)(BOOL success))completion;

// 清除指定目录及子目录下所有文件
+ (void)cleanFromDirectory:(NSString *)directory completion:(void(^)(BOOL success))completion;

@end
