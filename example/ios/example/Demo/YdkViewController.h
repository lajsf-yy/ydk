//
//  YdkViewController.h
//  example
//
//  Created by yryz on 2019/6/24.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <UIKit/UIKit.h>
#import <ydk-core/YdkModule.h>

@interface YdkViewController : UIViewController

@end

@interface YdkNetworkInterceptor : NSObject <YdkModule>

@end

