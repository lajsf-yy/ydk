//
//  YdkToolkit.h
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import <Foundation/Foundation.h>

#if __has_include(<ydk-toolkit/YdkToolkit.h>)

#import <ydk-toolkit/YdkToolkitMacros.h>

#import <ydk-toolkit/YdkKeychain.h>
#import <ydk-toolkit/YdkImageTool.h>
#import <ydk-toolkit/YdkVideoTool.h>
#import <ydk-toolkit/YdkFileSystemTool.h>

#import <ydk-toolkit/YdkAlertView.h>

#import <ydk-toolkit/NSData+toolkit.h>
#import <ydk-toolkit/NSString+toolkit.h>
#import <ydk-toolkit/NSURL+toolkit.h>
#import <ydk-toolkit/UIColor+toolkit.h>
#import <ydk-toolkit/UIDevice+toolkit.h>
#import <ydk-toolkit/UIImage+toolkit.h>
#import <ydk-toolkit/UILabel+toolkit.h>
#import <ydk-toolkit/UIView+toolkit.h>
#import <ydk-toolkit/UIView+toast.h>
#import <ydk-toolkit/UIViewController+toolkit.h>
#import <ydk-toolkit/AVPlayerItem+toolkit.h>
#import <ydk-toolkit/YdkVideoInfo.h>

#else

#import "YdkToolkitMacros.h"

#import "YdkKeychain.h"
#import "YdkImageTool.h"
#import "YdkVideoTool.h"
#import "YdkFileSystemTool.h"

#import "YdkAlertView.h"

#import "NSData+toolkit.h"
#import "NSString+toolkit.h"
#import "NSURL+toolkit.h"
#import "UIColor+toolkit.h"
#import "UIDevice+toolkit.h"
#import "UIImage+toolkit.h"
#import "UILabel+toolkit.h"
#import "UIView+toolkit.h"
#import "UIView+toast.h"
#import "UIViewController+toolkit.h"
#import "AVPlayerItem+toolkit.h"

#import "YdkVideoInfo.h"

#endif
