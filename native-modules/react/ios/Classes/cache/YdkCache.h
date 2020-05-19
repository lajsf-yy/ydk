#import <ydk-core/YdkCore.h>

@interface YdkCache : NSObject

- (NSUInteger)getCacheSizeForCaches;
- (NSUInteger)getCacheSizeForTmp;
- (NSUInteger)getCacheSize;
- (void)clearCacheForCaches;
- (void)clearCacheForTmp;
- (void)clearCache;

@end
