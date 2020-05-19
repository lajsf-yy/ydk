//
//  YdkQRCodeView.m
//  ydk-qrcode
//
//  Created by yryz on 2019/8/19.
//

#import "YdkQRCodeView.h"

#import <AVFoundation/AVFoundation.h>
#import <ydk-toolkit/YdkToolkit.h>
#import <ydk-permission/YdkPermissionManager.h>

NSErrorDomain const YdkQRCodeViewErrorDomain = @"YdkQRCodeViewErrorDomain";

static const CGFloat kCornerW = 18.0;
static const CGFloat kCornerLineW = 3.0;

// 角位置
typedef NS_ENUM(NSInteger, CornerPosition) {
    CornerPositionLeftTop = 0,
    CornerPositionLeftBottom,
    CornerPositionRightTop,
    CornerPositionRightBottom
};

@interface YdkQRCodeView () <AVCaptureMetadataOutputObjectsDelegate, AVCaptureVideoDataOutputSampleBufferDelegate>

@property (nonatomic, strong) UIView *maskView;
@property (nonatomic, strong) UIView *scanView;
@property (nonatomic, strong) UIView *scanLineView;
@property (nonatomic, strong) UIButton *flashlightBtn;
@property (readwrite) BOOL flashlighOn;

@property (nonatomic, strong) AVCaptureSession *session;

@end

@implementation YdkQRCodeView
{
    BOOL _isReady;
    CGRect _scanRect;
    
    // 四个角
    CAShapeLayer *_leftTopLayer;
    CAShapeLayer *_leftBottomLayer;
    CAShapeLayer *_rightTopLayer;
    CAShapeLayer *_rightBottomLayer;
    // 渐变
    CAGradientLayer *_gradientLayer;
    // 预览层
    AVCaptureVideoPreviewLayer *_preLayer;
}

- (instancetype)initWithFrame:(CGRect)frame {
    if (self = [super initWithFrame:frame]) {
        _scanSize = CGSizeMake(frame.size.width * 2.0 / 3.0, frame.size.width * 2.0 / 3.0);
        _scanTopMargin = MAX((frame.size.height - _scanSize.height) / 2.0, 0);
        _scanRect = CGRectMake((frame.size.width - _scanSize.width) / 2.0, _scanTopMargin, _scanSize.width, _scanSize.height);
        _scanLineColor = [UIColor whiteColor];
        _scanCornerColor = [UIColor whiteColor];
        _scanBorderColor = [UIColor clearColor];
        [self setupUI];
        dispatch_async(dispatch_get_main_queue(), ^{
            [self setupScanning];
        });
        
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationDidEnterBackground:) name:UIApplicationDidEnterBackgroundNotification object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(applicationWillEnterForeground:) name:UIApplicationWillEnterForegroundNotification object:nil];
    }
    return self;
}

// MARK: - SetNeedsLayout
- (void)setScanTopMargin:(CGFloat)scanTopMargin {
    _scanTopMargin = scanTopMargin;
    [self setNeedsLayout];
}

- (void)setScanSize:(CGSize)scanSize {
    _scanSize = scanSize;
    [self setNeedsLayout];
}

- (void)setScanLineColor:(UIColor *)scanLineColor {
    _scanLineColor = scanLineColor;
    [self setNeedsLayout];
}

- (void)setScanCornerColor:(UIColor *)scanCornerColor {
    _scanCornerColor = scanCornerColor;
    [self setNeedsLayout];
}

- (void)setScanBorderColor:(UIColor *)scanBorderColor {
    _scanBorderColor = scanBorderColor;
    [self setNeedsLayout];
}

- (void)layoutSubviews {
    [super layoutSubviews];
    CGRect frame = self.bounds;
    _preLayer.frame = frame;
    if (CGSizeEqualToSize(_scanSize, CGSizeZero)) {
        _scanSize = CGSizeMake(frame.size.width * 2.0 / 3.0, frame.size.width * 2.0 / 3.0);
    }
    if (_scanTopMargin == 0) {
        _scanTopMargin = MAX((frame.size.height - _scanSize.height) / 2.0, 0);
    }
    _scanRect = CGRectMake((frame.size.width - _scanSize.width) / 2.0, _scanTopMargin, _scanSize.width, _scanSize.height);
    
    // Mask layer
    _maskView.frame = self.bounds;
    UIBezierPath *path = [UIBezierPath bezierPathWithRect:_maskView.bounds];
    [path appendPath:[[UIBezierPath bezierPathWithRect:_scanRect] bezierPathByReversingPath]];
    CAShapeLayer *shapeLayer = [CAShapeLayer layer];
    shapeLayer.backgroundColor = [UIColor whiteColor].CGColor;
    shapeLayer.path = path.CGPath;
    [_maskView.layer setMask:shapeLayer];
    
    _scanView.frame = _scanRect;
    _scanView.layer.borderColor = _scanBorderColor.CGColor;
    _scanLineView.frame = CGRectMake(0, 0, _scanRect.size.width, 2);
    [self updateGradientLayer];
    
    // Scan corner
    _leftTopLayer.position = [self cornerPositionWithPosition:CornerPositionLeftTop scanRect:_scanRect cornerWidth:kCornerW];
    [self setupCornerLayer:_leftTopLayer lineWidth:kCornerLineW strokeColor:_scanCornerColor];
    
    _leftBottomLayer.position = [self cornerPositionWithPosition:CornerPositionLeftBottom scanRect:_scanRect cornerWidth:kCornerW];
    [self setupCornerLayer:_leftBottomLayer lineWidth:kCornerLineW strokeColor:_scanCornerColor];
    
    _rightTopLayer.position = [self cornerPositionWithPosition:CornerPositionRightTop scanRect:_scanRect cornerWidth:kCornerW];
    [self setupCornerLayer:_rightTopLayer lineWidth:kCornerLineW strokeColor:_scanCornerColor];
    
    _rightBottomLayer.position = [self cornerPositionWithPosition:CornerPositionRightBottom scanRect:_scanRect cornerWidth:kCornerW];
    [self setupCornerLayer:_rightBottomLayer lineWidth:kCornerLineW strokeColor:_scanCornerColor];
}

// MARK: - UI
- (void)setupUI {
    UIView *maskView = [[UIView alloc] initWithFrame:self.bounds];
    maskView.backgroundColor = [UIColor colorWithWhite:0.f alpha:.3f];
    [self addSubview:_maskView = maskView];
    
    UIView *scanView = [[UIView alloc] initWithFrame:_scanRect];
    scanView.layer.borderWidth = 1 / [UIScreen mainScreen].scale;
    
    UIView *scanLineView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, _scanRect.size.width, 1)];
    scanLineView.backgroundColor = [UIColor clearColor];
    [scanView addSubview:_scanLineView = scanLineView];
    [self addSubview:_scanView = scanView];
    
    CAGradientLayer *gradientLayer = [CAGradientLayer layer];
    gradientLayer.locations = @[@0.0, @0.3, @0.7, @1.0];
    gradientLayer.startPoint = CGPointMake(0, 0.5);
    gradientLayer.endPoint = CGPointMake(1, 0.5);
    [scanLineView.layer addSublayer:_gradientLayer = gradientLayer];
    
    [self setupScanCorner];
}

- (void)setupScanCorner {
    CGFloat cornerW = kCornerW;
    CGFloat cornerLineW = kCornerLineW;
    //左上
    CAShapeLayer *leftTopLayer = [CAShapeLayer new];
    leftTopLayer.path = [self cornerPathWithPosition:CornerPositionLeftTop cornerWidth:cornerW lineWidth:cornerLineW].CGPath;
    [_scanView.layer addSublayer:_leftTopLayer = leftTopLayer];
    
    //左下
    CAShapeLayer *leftBottomLayer = [CAShapeLayer new];
    leftBottomLayer.path = [self cornerPathWithPosition:CornerPositionLeftBottom cornerWidth:cornerW lineWidth:cornerLineW].CGPath;
    [_scanView.layer addSublayer:_leftBottomLayer = leftBottomLayer];
    
    //右上
    CAShapeLayer *rightTopLayer = [CAShapeLayer new];
    rightTopLayer.path = [self cornerPathWithPosition:CornerPositionRightTop cornerWidth:cornerW lineWidth:cornerLineW].CGPath;
    [_scanView.layer addSublayer:_rightTopLayer = rightTopLayer];
    
    //右下
    CAShapeLayer *rightBottomLayer = [CAShapeLayer new];
    rightBottomLayer.path = [self cornerPathWithPosition:CornerPositionRightBottom cornerWidth:cornerW lineWidth:cornerLineW].CGPath;
    [_scanView.layer addSublayer:_rightBottomLayer = rightBottomLayer];
}

// MARK: - Utils

// 获取四个角的path
- (UIBezierPath *)cornerPathWithPosition:(CornerPosition)position cornerWidth:(CGFloat)cornerW lineWidth:(CGFloat)lineW {
    UIBezierPath *path = [UIBezierPath new];
    switch (position) {
        case CornerPositionLeftTop: {
            [path moveToPoint:CGPointMake(lineW , cornerW)];
            [path addLineToPoint:CGPointMake(lineW, lineW)];
            [path addLineToPoint:CGPointMake(cornerW, lineW)];
        }
            break;
        case CornerPositionLeftBottom: {
            [path moveToPoint:CGPointMake(lineW, lineW)];
            [path addLineToPoint:CGPointMake(lineW, cornerW)];
            [path addLineToPoint:CGPointMake(cornerW, cornerW)];
        }
            break;
        case CornerPositionRightTop: {
            [path moveToPoint:CGPointMake(lineW, lineW)];
            [path addLineToPoint:CGPointMake(cornerW, lineW)];
            [path addLineToPoint:CGPointMake(cornerW, cornerW)];
        }
            
            break;
        case CornerPositionRightBottom: {
            [path moveToPoint:CGPointMake(cornerW, lineW)];
            [path addLineToPoint:CGPointMake(cornerW, cornerW)];
            [path addLineToPoint:CGPointMake(lineW, cornerW)];
        }
            break;
    }
    return path;
}

// 获取四个角的position
- (CGPoint)cornerPositionWithPosition:(CornerPosition)position scanRect:(CGRect)scanRect cornerWidth:(CGFloat)cornerW {
    CGPoint point;
    switch (position) {
        case CornerPositionLeftTop:
            point = CGPointMake(cornerW / 2 - kCornerLineW, cornerW / 2 - kCornerLineW);
            break;
        case CornerPositionLeftBottom:
            point = CGPointMake(cornerW / 2 - kCornerLineW, scanRect.size.height - cornerW / 2 + kCornerLineW);
            break;
        case CornerPositionRightTop:
            point = CGPointMake(scanRect.size.width - cornerW / 2 + kCornerLineW, cornerW / 2 - kCornerLineW);
            break;
        case CornerPositionRightBottom:
            point = CGPointMake(scanRect.size.width - cornerW / 2 + kCornerLineW, scanRect.size.height - cornerW / 2 + kCornerLineW);
            break;
    }
    return point;
}

// 填充颜色/宽度
- (void)setupCornerLayer:(CAShapeLayer *)layer lineWidth:(CGFloat)width strokeColor:(UIColor *)strokeColor {
    layer.lineWidth = width;
    layer.strokeColor = strokeColor.CGColor;
    layer.fillColor = [UIColor clearColor].CGColor;
    
    CGPathRef bound = CGPathCreateCopyByStrokingPath(layer.path, nil, layer.lineWidth, kCGLineCapRound, kCGLineJoinMiter, layer.miterLimit);
    layer.bounds = CGPathGetBoundingBox(bound);
    CGPathRelease(bound);
}

// MARK: - Setup scanning
- (void)setupScanning {
    @weakify(self);
    // 判断权限
    [[[YdkPermissionManager requestAuthorization:YdkPermissionAuthorizationTypeCamera]
      deliverOn:[RACScheduler mainThreadScheduler]]
     subscribeNext:^(NSNumber *x) {
         @strongify(self);
         YdkPermissionAuthorizationStatus status = [x integerValue];
         if (status == YdkPermissionAuthorizationStatusAuthorized) {
             [self _setupScanning];
         } else {
             if (self.errorBlock) {
                 NSError *error = [NSError errorWithDomain:YdkQRCodeViewErrorDomain code:YdkQRCodeViewErrorCameraAuthorizationDenied userInfo:@{NSLocalizedDescriptionKey : @"设备无摄像头权限"}];
                 self.errorBlock(error);
             }
         }
     } error:^(NSError *error) {
         @strongify(self);
         if (self.errorBlock) {
             self.errorBlock(error);
         }
     }];
}

- (void)_setupScanning {
    AVCaptureDevice *device = [AVCaptureDevice defaultDeviceWithMediaType:AVMediaTypeVideo];
    if (!device) {
        if (_errorBlock) {
            _errorBlock([NSError errorWithDomain:YdkQRCodeViewErrorDomain code:YdkQRCodeViewErrorCameraAuthorizationDenied userInfo:@{NSLocalizedDescriptionKey : @"设备无摄像头权限"}]);
        }
        return;
    }
    
    NSError *error;
    AVCaptureDeviceInput *input = [AVCaptureDeviceInput deviceInputWithDevice:device error:&error];
    if (error || !input) {
        if (_errorBlock) {
            _errorBlock([NSError errorWithDomain:YdkQRCodeViewErrorDomain code:YdkQRCodeViewErrorCameraOpenFailed userInfo:@{NSLocalizedDescriptionKey : @"无法开启摄像头"}]);
        }
        return;
    }
    _isReady = YES;
    AVCaptureMetadataOutput *output = [[AVCaptureMetadataOutput alloc] init];
    [output setMetadataObjectsDelegate:self queue:dispatch_get_main_queue()];
    _session = [[AVCaptureSession alloc] init];
    if ([UIScreen mainScreen].bounds.size.height < 500) {
        _session.sessionPreset = AVCaptureSessionPreset640x480;
    } else {
        _session.sessionPreset = AVCaptureSessionPresetHigh;
    }
    // 实时监听光线亮度值
    AVCaptureVideoDataOutput *videoDataOutput = [[AVCaptureVideoDataOutput alloc] init];
    [videoDataOutput setSampleBufferDelegate:self queue:dispatch_get_main_queue()];
    
    [_session addInput:input];
    [_session addOutput:output];
    [_session addOutput:videoDataOutput];
    // 扫码编码格式
    output.metadataObjectTypes = @[AVMetadataObjectTypeQRCode, AVMetadataObjectTypeEAN8Code, AVMetadataObjectTypeEAN13Code, AVMetadataObjectTypeCode128Code];
    // 扫描区域
    CGRect rect = [self fetchScanRect:_scanView.frame];
    output.rectOfInterest = rect;
    // 预览层
    AVCaptureVideoPreviewLayer *preLayer = [AVCaptureVideoPreviewLayer layerWithSession:_session];
    preLayer.videoGravity = AVLayerVideoGravityResizeAspectFill;
    preLayer.frame = self.layer.bounds;
    [self.layer insertSublayer:_preLayer = preLayer atIndex:0];
    [_session startRunning];
    
    [self startScanning];
}

- (CGRect)fetchScanRect:(CGRect)originRect {
    CGSize size = self.bounds.size;
    CGRect rect = CGRectMake(originRect.origin.y / size.height, originRect.origin.x / size.width, originRect.size.height / size.height, originRect.size.width / size.width);
    return rect;
}

#define TRANSLATION_ANIMATION @"TranslationAnimation"
- (void)resumeAnimation {
    CABasicAnimation *animation = [_scanLineView.layer animationForKey:TRANSLATION_ANIMATION];
    if (animation) {
//        [_scanLineView.layer removeAnimationForKey:TRANSLATION_ANIMATION];
        CFTimeInterval pauseTime = _scanLineView.layer.timeOffset;
        CFTimeInterval beginTime = CACurrentMediaTime() - pauseTime;
        _scanLineView.layer.timeOffset = 0;
        _scanLineView.layer.beginTime = beginTime;
        _scanLineView.layer.speed = 1;
    } else {
        animation = [CABasicAnimation animation];
        animation.keyPath = @"transform.translation.y";
        animation.byValue = @(_scanSize.height);
        animation.duration = 3.0;
        animation.repeatCount = INFINITY;
        [_scanLineView.layer addAnimation:animation forKey:TRANSLATION_ANIMATION];
    }
}

- (void)startScanning {
    if (_isReady) {
        [self resumeAnimation];
        if (!_session.isRunning) {
            [_session startRunning];
        }
    }
}

- (void)stopScanning {
    [_scanLineView.layer removeAnimationForKey:TRANSLATION_ANIMATION];
    [_session stopRunning];
}

// MARK: - Flashlight
- (void)flashlighAction {
    AVCaptureDevice *captureDevice;
    NSArray<AVCaptureDevice *> *devices = [AVCaptureDevice devices];
    for (AVCaptureDevice *device in devices) {
        if ([device hasMediaType:AVMediaTypeVideo]) {
            if (device.position == AVCaptureDevicePositionBack) {
                captureDevice = device;
            }
        }
    }
    if (!captureDevice) {
        return;
    }
    
    if (captureDevice.hasTorch && captureDevice.hasFlash) {
        NSError *error;
        [captureDevice lockForConfiguration:&error];
        if (error) {
            return;
        }
        if (_flashlighOn) {
            captureDevice.torchMode = AVCaptureTorchModeOff;
            captureDevice.flashMode = AVCaptureFlashModeOff;
        } else {
            captureDevice.torchMode = AVCaptureTorchModeOn;
            captureDevice.flashMode = AVCaptureFlashModeOn;
        }
        self.flashlighOn = !_flashlighOn;
        [captureDevice unlockForConfiguration];
    }
}

// MARK: - AVCaptureMetadataOutputObjectsDelegate
- (void)captureOutput:(AVCaptureOutput *)output didOutputMetadataObjects:(NSArray<__kindof AVMetadataObject *> *)metadataObjects fromConnection:(AVCaptureConnection *)connection {
    if (metadataObjects.count == 0) {
        return;
    }
    AVMetadataObject *metadataObject = [metadataObjects objectAtIndex:0];
    if (metadataObject && [metadataObject isKindOfClass:[AVMetadataMachineReadableCodeObject class]]) {
        NSString *code = [((AVMetadataMachineReadableCodeObject *)metadataObject) stringValue];
        [self handleScanResult:code];
        [self stopScanning];
    }
}

// 实时监听亮度值
- (void)captureOutput:(AVCaptureOutput *)output didOutputSampleBuffer:(CMSampleBufferRef)sampleBuffer fromConnection:(AVCaptureConnection *)connection {
    
    CFDictionaryRef metadataDict = CMCopyDictionaryOfAttachments(NULL,sampleBuffer, kCMAttachmentMode_ShouldPropagate);
    NSDictionary *metadata = [[NSMutableDictionary alloc] initWithDictionary:(__bridge NSDictionary*)metadataDict];
    CFRelease(metadataDict);
    NSDictionary *exifMetadata = [[metadata objectForKey:(NSString *)kCGImagePropertyExifDictionary] mutableCopy];
    
    // 亮度值
    float brightnessValue = [[exifMetadata objectForKey:(NSString *)kCGImagePropertyExifBrightnessValue] floatValue];
    if (self.brightnessBlock) {
        self.brightnessBlock(brightnessValue);
    }
}

- (void)handleScanResult:(NSString *)result {
    if (result.length > 0) {
        AudioServicesPlaySystemSound(kSystemSoundID_Vibrate); // 震动反馈
        if (_scanResult) {
//            NSData *data = [result dataUsingEncoding:NSUTF8StringEncoding];
//            NSError *error;
//            NSDictionary *info = [NSJSONSerialization JSONObjectWithData:data options:NSJSONReadingMutableContainers error:&error];
            _scanResult(result);
        }
    }
}

- (void)updateGradientLayer {
    UIColor *tmpColor = [_scanLineColor colorWithAlphaComponent:0.0];
    CGColorRef color1 = tmpColor.CGColor;
    CGColorRef color2 = _scanLineColor.CGColor;
    CGColorRef color3 = _scanLineColor.CGColor;
    CGColorRef color4 = tmpColor.CGColor;
    _gradientLayer.frame = _scanLineView.bounds;
    _gradientLayer.colors = @[(__bridge id)color1, (__bridge id)color2, (__bridge id)color3, (__bridge id)color4];
}

// MARK: - NSNotificationName
- (void)applicationDidEnterBackground:(UIApplication *)application {
    [self stopScanning];
}

- (void)applicationWillEnterForeground:(UIApplication *)application {
    [self startScanning];
}

@end
