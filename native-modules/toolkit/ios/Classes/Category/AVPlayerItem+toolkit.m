//
//  AVPlayerItem+toolkit.m
//  ydk-toolkit
//
//  Created by yryz on 2019/7/15.
//

#import "AVPlayerItem+toolkit.h"

@implementation AVPlayerItem (toolkit)

- (Float64)tk_calculatePlayableDuration {
    if (self.status == AVPlayerItemStatusReadyToPlay) {
        __block CMTimeRange effectiveTimeRange;
        [self.loadedTimeRanges enumerateObjectsUsingBlock:^(id obj, NSUInteger idx, BOOL *stop) {
            CMTimeRange timeRange = [obj CMTimeRangeValue];
            if (CMTimeRangeContainsTime(timeRange, self.currentTime)) {
                effectiveTimeRange = timeRange;
                *stop = YES;
            }
        }];
        Float64 playableDuration = CMTimeGetSeconds(CMTimeRangeGetEnd(effectiveTimeRange));
        if (playableDuration > 0) {
            return playableDuration;
        }
    }
    return 0;
}

@end
