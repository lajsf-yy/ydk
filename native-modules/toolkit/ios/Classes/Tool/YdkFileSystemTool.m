//
//  YdkFileSystemTool.m
//  ydk-toolkit
//
//  Created by yryz on 2019/7/4.
//

#import "YdkFileSystemTool.h"

@implementation YdkFileSystemTool
{
    NSFileManager *_fileManager;
    dispatch_queue_t _ioQueue;
}

+ (YdkFileSystemTool *)manager {
    static YdkFileSystemTool *_manager = nil;
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken,^{
        _manager = [[YdkFileSystemTool alloc] init];
    });
    return _manager;
}

- (instancetype)init {
    if (self = [super init]) {
        _fileManager = [NSFileManager defaultManager];
        _ioQueue = dispatch_queue_create("com.yryz.ydk.fileSystem", DISPATCH_QUEUE_SERIAL);
    }
    return self;
}

+ (NSString *)documentDirectoryForComponent:(NSString *)component {
    NSString *subdir = [self escapedResourceName:component];
    return [[NSSearchPathForDirectoriesInDomains(NSDocumentDirectory, NSUserDomainMask, YES).firstObject
             stringByAppendingPathComponent:@"ydk"]
            stringByAppendingPathComponent:subdir];
}

+ (NSString *)cachesDirectoryForComponent:(NSString *)component {
    NSString *subdir = [self escapedResourceName:component];
    return [[NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject
             stringByAppendingPathComponent:@"ydk"]
            stringByAppendingPathComponent:subdir];
}

+ (NSString *)tempDirectoryForComponent:(NSString *)component {
    NSString *subdir = [self escapedResourceName:component];
    return [[NSTemporaryDirectory() stringByAppendingPathComponent:@"ydk"] stringByAppendingPathComponent:subdir];
}

// MARK: - Create Method
+ (NSURL *)createCacheFileURLWithComponent:(NSString *)component {
    return [self createCacheFileURLWithComponent:component fileName:nil];
}

+ (NSURL *)createCacheFileURLWithComponent:(NSString *)component fileName:(NSString *)fileName {
    NSString *filePath = [[self cachesDirectoryForComponent:component] stringByAppendingPathComponent:fileName ? : @""];
    return [self createFileURLWithFilePath:filePath];
}

+ (NSURL *)createTempFileURLWithComponent:(NSString *)component {
    return [self createTempFileURLWithComponent:component fileName:nil];
}

+ (NSURL *)createTempFileURLWithComponent:(NSString *)component fileName:(NSString *)fileName {
    NSString *filePath = [[self tempDirectoryForComponent:component] stringByAppendingPathComponent:fileName ? : @""];
    return [self createFileURLWithFilePath:filePath];
}

+ (NSURL *)createFileURLWithFilePath:(NSString *)filePath {
    NSString *directory = filePath;
    if (filePath.pathExtension.length > 0) {
        directory = [filePath stringByDeletingLastPathComponent];
    }
    if (![[NSFileManager defaultManager] fileExistsAtPath:directory]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:directory withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    BOOL isDirectory;
    BOOL isExists = [[NSFileManager defaultManager] fileExistsAtPath:filePath isDirectory:&isDirectory];
    if (isExists && !isDirectory) {
        [[NSFileManager defaultManager] removeItemAtPath:filePath error:nil];
    }
    return [NSURL fileURLWithPath:filePath];
}

+ (NSString *)escapedResourceName:(NSString *)name {
    NSString *charactersToEscape = @"!*'();:@&=+$,/?%#[]";
    NSCharacterSet *allowedCharacters = [[NSCharacterSet characterSetWithCharactersInString:charactersToEscape] invertedSet];
    return [name stringByAddingPercentEncodingWithAllowedCharacters:allowedCharacters];
}

// 唯一文件名[[NSProcessInfo processInfo] globallyUniqueString]]

// MARK: - Fetch
+ (unsigned long long)cacheSize {
    NSString *cachePath = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
    return [self fileSizeFromDirectory:cachePath];
}

// 获取指定目录及子目录总大小
+ (unsigned long long)fileSizeFromDirectory:(NSString *)directory {
    return [[self manager] fileSizeFromDirectory:directory];
}

// MARK: - Cache
+ (void)cleanCache {
    [self cleanCacheWithCompletionBlock:nil];
}

+ (void)cleanCacheWithCompletionBlock:(void(^)(BOOL success))completion {
    NSString *cachePath = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject;
    [self cleanFromDirectory:cachePath completion:completion];
}

// 清除指定目录及子目录下所有文件
+ (void)cleanFromDirectory:(NSString *)directory completion:(void(^)(BOOL success))completion {
    [[self manager] cleanFromDirectory:directory completion:completion];
}

// MARK: - instance method
// 获取指定目录及子目录总大小
- (unsigned long long)fileSizeFromDirectory:(NSString *)directory {
    __block NSUInteger size = 0;
    dispatch_sync(self->_ioQueue, ^{
        NSDirectoryEnumerator *fileEnumeratorCache = [self->_fileManager enumeratorAtPath:directory];
        for (NSString *fileName in fileEnumeratorCache) {
            NSString *filePath = [directory stringByAppendingPathComponent:fileName];
            NSDictionary *attrs = [self->_fileManager attributesOfItemAtPath:filePath error:nil];
            if ([attrs.fileType isEqualToString:NSFileTypeDirectory]) continue;
            size += [attrs fileSize];
        }
    });
    return size;
}

// 清除指定目录及子目录下所有文件
- (void)cleanFromDirectory:(NSString *)directory completion:(void(^)(BOOL success))completion {
    dispatch_async(self->_ioQueue, ^{
        NSError *error;
        NSDirectoryEnumerator *fileEnumeratorCache = [self->_fileManager enumeratorAtPath:directory];
        for (NSString *fileName in fileEnumeratorCache) {
            NSString *filePath = [directory stringByAppendingPathComponent:fileName];
            if ([self->_fileManager fileExistsAtPath:filePath]) {
                [self->_fileManager removeItemAtPath:filePath error:&error];
            }
        }
        if (completion) {
            dispatch_async(dispatch_get_main_queue(), ^{
                completion(YES);
            });
        }
    });
}

@end
