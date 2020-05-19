//
//  YdkVideoPlayerViewManager.m
//  ydk-video
//
//  Created by yryz on 2019/7/15.
//

#import "YdkVideoPlayerViewManager.h"
#import "YdkVideoPlayerReactView.h"

#import <React/RCTUIManager.h>

@implementation YdkVideoPlayerViewManager

RCT_EXPORT_MODULE(YdkVideoPlayView)

- (UIView *)view {
    return [[YdkVideoPlayerReactView alloc] initWithEventDispatcher:self.bridge.eventDispatcher];
}

+ (BOOL)requiresMainQueueSetup {
    return YES;
}

- (dispatch_queue_t)methodQueue {
    return self.bridge.uiManager.methodQueue;
}

RCT_REMAP_VIEW_PROPERTY(source, videoView.source, NSDictionary)

RCT_EXPORT_VIEW_PROPERTY(onReadyToPlay, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onVideoLoad, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onVideoLoadEnd, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onPlayError, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onPlayEnd, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onVideoProgress, RCTDirectEventBlock)

RCT_EXPORT_METHOD(start:(nonnull NSNumber *)reactTag)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, YdkVideoPlayerReactView *> *viewRegistry) {
        YdkVideoPlayerReactView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[YdkVideoPlayerReactView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting YdkVideoPlayerReactView, got: %@", view);
        } else {
            [view.videoView play];
        }
    }];
}

RCT_EXPORT_METHOD(pause:(nonnull NSNumber *)reactTag)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, YdkVideoPlayerReactView *> *viewRegistry) {
        YdkVideoPlayerReactView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[YdkVideoPlayerReactView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting YdkVideoPlayerReactView, got: %@", view);
        } else {
            [view.videoView pause];
        }
    }];
}
// - (void)seekToTime:(NSTimeInterval)time;
RCT_EXPORT_METHOD(seekToTime:(nonnull NSNumber *)reactTag time:(nonnull NSNumber *)time)
{
    [self.bridge.uiManager addUIBlock:^(__unused RCTUIManager *uiManager, NSDictionary<NSNumber *, YdkVideoPlayerReactView *> *viewRegistry) {
        YdkVideoPlayerReactView *view = viewRegistry[reactTag];
        if (![view isKindOfClass:[YdkVideoPlayerReactView class]]) {
            RCTLogError(@"Invalid view returned from registry, expecting YdkVideoPlayerReactView, got: %@", view);
        } else {
            [view.videoView seekToTime:time.doubleValue];
        }
    }];
}

@end
