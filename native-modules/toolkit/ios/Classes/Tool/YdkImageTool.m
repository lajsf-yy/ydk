//
//  YdkImageTool.m
//  ydk-toolkit
//
//  Created by yryz on 2019/7/4.
//

#import "YdkImageTool.h"

NSErrorDomain const YdkImageToolErrorDomain = @"YdkImageToolErrorDomain";

@implementation YdkImageTool

+ (BOOL)saveImageURL:(NSURL *)imageURL targetURL:(NSURL *)targetURL {
    UIImage *image = [UIImage imageWithData:[NSData dataWithContentsOfURL:imageURL]];
    return [self saveImage:image targetURL:targetURL];
}

+ (BOOL)saveImage:(id)image targetURL:(NSURL *)targetURL; {
    NSAssert(targetURL, @"targetURL can’t be nil");
    
    NSData *imageData;
    if ([image isKindOfClass:[NSData class]]) {
        imageData = (NSData *)image;
    } else {
        imageData = UIImagePNGRepresentation(image);
    }
    if (imageData.length > 0) {
        NSString *filePath = targetURL.path;
        NSString *directory = [targetURL.path stringByDeletingLastPathComponent];
        if (![[NSFileManager defaultManager] fileExistsAtPath:directory]) {
            [[NSFileManager defaultManager] createDirectoryAtPath:directory withIntermediateDirectories:YES attributes:nil error:nil];
        }
        if ([[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
            [[NSFileManager defaultManager] removeItemAtPath:filePath error:nil];
        }
        BOOL result = [NSFileManager.defaultManager createFileAtPath:filePath contents:imageData attributes:nil];
        return result;
    } else {
        return NO;
    }
}

@end
