//
//  MCAVPlayerItemCacheLoader.h
//  AVPlayerCacheSupport
//
//  Created by yryz on 19/3/21.
//  Copyright © 2019年 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <AVFoundation/AVAssetResourceLoader.h>

@interface MCAVPlayerItemCacheLoader : NSObject<AVAssetResourceLoaderDelegate>

@property (nonatomic,readonly) NSString *cacheFilePath;

+ (instancetype)cacheLoaderWithCacheFilePath:(NSString *)cacheFilePath;
- (instancetype)initWithCacheFilePath:(NSString *)cacheFilePath;
+ (void)removeCacheWithCacheFilePath:(NSString *)cacheFilePath;
@end
