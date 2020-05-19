//
//  YdkAlbum.h
//  ydk-album
//
//  Created by yryz on 2019/7/4.
//

#import <Foundation/Foundation.h>
#import <ReactiveObjC/ReactiveObjC.h>
#import "YdkAlbumPickerConfig.h"

FOUNDATION_EXPORT NSErrorDomain const YdkAlbumErrorDomain;
NS_ERROR_ENUM(YdkAlbumErrorDomain)
{
    YdkAlbumErrorDomainNotFoundCurrentViewController        = -1000,     // 未找到当前控制器
};

@interface YdkAlbum : NSObject

+ (RACSignal<NSArray/*[FilePath] or YVideoInfo*/ *> *)presentImagePickerWithConfig:(YdkAlbumPickerConfig *)config sourceViewController:(UIViewController *)sourceVC;

@end

