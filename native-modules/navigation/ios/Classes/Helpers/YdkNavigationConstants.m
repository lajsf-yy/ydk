//
//  YdkConstants.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkNavigationConstants.h"

@implementation YdkNavigationConstants

+ (NSDictionary *)getConstants {
  return @{@"topBarHeight": @([self topBarHeight]), @"statusBarHeight": @([self statusBarHeight]), @"bottomTabsHeight": @([self bottomTabsHeight])};
}

+ (CGFloat)topBarHeight {
  return UIApplication.sharedApplication.delegate.window.rootViewController.navigationController.navigationBar.frame.size.height;
}

+ (CGFloat)statusBarHeight {
  return [UIApplication sharedApplication].statusBarFrame.size.height;
}

+ (CGFloat)bottomTabsHeight {
  return CGRectGetHeight(((UITabBarController *)((UIWindow *)(UIApplication.sharedApplication.windows[0])).rootViewController).tabBar.frame);
}

@end
