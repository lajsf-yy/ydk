//
//  YdkQRCodeView.h
//  ydk-qrcode
//
//  Created by yryz on 2019/8/19.
//

#import <UIKit/UIKit.h>

FOUNDATION_EXPORT NSErrorDomain const YdkQRCodeViewErrorDomain;

NS_ERROR_ENUM(YdkQRCodeViewErrorDomain)
{
    YdkQRCodeViewErrorCameraAuthorizationDenied    = -1000, // 设备无摄像头权限
    YdkQRCodeViewErrorCameraOpenFailed             = -1001, // 无法开启摄像头
};

@interface YdkQRCodeView : UIView

@property (nonatomic, assign) CGFloat scanTopMargin;    // default is vertically center
@property (nonatomic, assign) CGSize scanSize;          // default is (self.width * 2.0 / 3.0)

@property (nonatomic, strong) UIColor *scanLineColor;   // default is white
@property (nonatomic, strong) UIColor *scanCornerColor; // default is white
@property (nonatomic, strong) UIColor *scanBorderColor; // default is clear

@property (nonatomic, copy) void(^scanResult)(NSString *result);
@property (nonatomic, copy) void(^brightnessBlock)(float brightness);
@property (nonatomic, copy) void(^errorBlock)(NSError *error);

@property (readonly, nonatomic, assign) BOOL flashlighOn;

- (void)flashlighAction;

- (void)startScanning;
- (void)stopScanning;

@end
