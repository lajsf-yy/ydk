

#import "YdkImageViewManager.h"

#import <UIKit/UIKit.h>

#import <React/RCTConvert.h>

#import <React/RCTImageShadowView.h>
#import "YdkImageView.h"

#import <SDWebImage/SDImageCodersManager.h>
#import <SDWebImageWebPCoder/SDImageWebPCoder.h>

@implementation YdkImageViewManager

RCT_EXPORT_MODULE()

- (instancetype)init {
    if (self = [super init]) {
        SDImageWebPCoder *webPCoder = [SDImageWebPCoder sharedCoder];
        [[SDImageCodersManager sharedManager] addCoder:webPCoder];
    }
    return self;
}

+ (BOOL)requiresMainQueueSetup {
    return true;
}

- (RCTShadowView *)shadowView
{
  return [RCTImageShadowView new];
}

- (UIView *)view
{
  return [[YdkImageView alloc] initWithBridge:self.bridge];
}

RCT_EXPORT_VIEW_PROPERTY(blurRadius, CGFloat)
RCT_EXPORT_VIEW_PROPERTY(capInsets, UIEdgeInsets)
RCT_REMAP_VIEW_PROPERTY(defaultSource, defaultImage, UIImage)
RCT_EXPORT_VIEW_PROPERTY(onLoadStart, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onProgress, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onError, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onPartialLoad, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onLoad, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(onLoadEnd, RCTDirectEventBlock)
RCT_EXPORT_VIEW_PROPERTY(resizeMode, RCTResizeMode)
RCT_REMAP_VIEW_PROPERTY(source, imageSources, NSArray<RCTImageSource *>);
RCT_CUSTOM_VIEW_PROPERTY(tintColor, UIColor, YdkImageView)
{
  // Default tintColor isn't nil - it's inherited from the superView - but we
  // want to treat a null json value for `tintColor` as meaning 'disable tint',
  // so we toggle `renderingMode` here instead of in `-[YdkImageView setTintColor:]`
  view.tintColor = [RCTConvert UIColor:json] ?: defaultView.tintColor;
  view.renderingMode = json ? UIImageRenderingModeAlwaysTemplate : defaultView.renderingMode;
}



@end
