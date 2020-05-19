#import "YdkCache.h"

@implementation YdkCache

+(void)load{
  ydk_register_module(self);
}

- (NSUInteger)getCacheSizeForCaches {
  NSFileManager *fileManager = [NSFileManager defaultManager];
  NSString *cachePath = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject];
  NSDirectoryEnumerator *fileEnumerator = [fileManager enumeratorAtPath:cachePath];
  NSUInteger totalCacheSize = 0;
  for (NSString *fileName in fileEnumerator) {
    NSString *filePath = [cachePath stringByAppendingPathComponent:fileName];
    NSDictionary *attrs = [fileManager attributesOfItemAtPath:filePath error:nil];
    totalCacheSize += [attrs fileSize];
  }
  return totalCacheSize;
}
- (NSUInteger)getCacheSizeForTmp {
  NSFileManager *fileManager = [NSFileManager defaultManager];
  NSString *tmpPath = NSTemporaryDirectory();
  NSDirectoryEnumerator *tempfileEnumerator = [fileManager enumeratorAtPath:tmpPath];
  NSUInteger totalTempSize = 0;
  for (NSString *fileName in tempfileEnumerator) {
    NSString *filePath = [tmpPath stringByAppendingPathComponent:fileName];
    NSDictionary *attrs = [fileManager attributesOfItemAtPath:filePath error:nil];
    totalTempSize += [attrs fileSize];
  }
  return totalTempSize;
}
- (NSUInteger)getCacheSize {
  NSUInteger totalCacheSize = [self getCacheSizeForCaches];
  NSUInteger totalTempSize = [self getCacheSizeForTmp];
  return totalCacheSize+totalTempSize;
}
- (void)clearCacheForCaches {
  NSFileManager *fileManager = [NSFileManager defaultManager];
  NSString *cachePath = [NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES) lastObject];
  NSDirectoryEnumerator *fileEnumerator = [fileManager enumeratorAtPath:cachePath];
  for (NSString *fileName in fileEnumerator) {
    NSString *filePath = [cachePath stringByAppendingPathComponent:fileName];
    [fileManager removeItemAtPath:filePath error:nil];
  }
}
- (void)clearCacheForTmp {
  NSFileManager *fileManager = [NSFileManager defaultManager];
  NSString *tmpPath = NSTemporaryDirectory();
  NSDirectoryEnumerator *tempfileEnumerator = [fileManager enumeratorAtPath:tmpPath];
  for (NSString *fileName in tempfileEnumerator) {
    NSString *filePath = [tmpPath stringByAppendingPathComponent:fileName];
    [fileManager removeItemAtPath:filePath error:nil];
  }
}
- (void)clearCache {
  [self clearCacheForCaches];
  [self clearCacheForTmp];
}

@end
