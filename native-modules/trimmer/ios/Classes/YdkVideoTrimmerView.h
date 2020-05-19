//
//  YdkVideoTrimmerView.h
//  ydk-trimmer
//
//  Created by yryz on 2019/9/4.
//

#import <UIKit/UIKit.h>
#import <AVKit/AVKit.h>
#import "YdkVideoTrimmerError.h"
#import "YdkTrimmerView.h"

@class YdkVideoInfo;

@interface YdkVideoTrimmerView : UIView

@property (nonatomic, strong) AVAsset *asset;

@property (nonatomic, assign) BOOL saveToPhotoAlbum;
@property (nonatomic, copy) UIColor *mainColor;

- (instancetype)initWithFrame:(CGRect)frame asset:(AVAsset *)asset NS_DESIGNATED_INITIALIZER;

- (void)cancel;
- (void)trim:(void (^)(NSError *error, YdkVideoInfo *video))complete;

@end
