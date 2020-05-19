//
//  MCAVPlayerItemRemoteCacheTask.h
//  AVPlayerCacheSupport
//
//  Created by yryz on 19/3/21.
//  Copyright © 2019年 yryz. All rights reserved.
//

#import "MCAVPlayerItemCacheTask.h"

@interface MCAVPlayerItemRemoteCacheTask : MCAVPlayerItemCacheTask

@property (nonatomic,strong) NSHTTPURLResponse *response;

@end
