//
//  UIViewController+toolkit.m
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import "UIViewController+toolkit.h"

@implementation UIViewController (toolkit)

+ (UIViewController *)currentViewController {
    UIViewController *result = nil;
    UIWindow *window = [[UIApplication sharedApplication] keyWindow];
    if (window.windowLevel != UIWindowLevelNormal) {
        NSArray *windows = [[UIApplication sharedApplication] windows];
        for (UIWindow *tmpWin in windows) {
            if (tmpWin.windowLevel == UIWindowLevelNormal) {
                window = tmpWin;
                break;
            }
        }
    }
    
    id nextResponder = nil;
    UIViewController *appRootVC = window.rootViewController;
    if (appRootVC.presentedViewController) {
        nextResponder = appRootVC.presentedViewController;
    } else {
        UIView *frontView = [[window subviews] objectAtIndex:0];
        nextResponder = [frontView nextResponder];
    }
    result = [self findViewController:nextResponder];
    return result;
}

+ (UIViewController *)findViewController:(UIViewController *)vc {
    UIViewController *result = vc;
    if ([vc isKindOfClass:[UITabBarController class]]) {
        UITabBarController *tabbar = (UITabBarController *)vc;
        UINavigationController *nav = (UINavigationController *)tabbar.viewControllers[tabbar.selectedIndex];
        result = nav.childViewControllers.lastObject;
    } else if ([vc isKindOfClass:[UINavigationController class]]) {
        UIViewController *nav = (UIViewController *)vc;
        result = nav.childViewControllers.lastObject;
    } else {
        if (![vc isKindOfClass:[UIViewController class]]) {
            if ([vc isKindOfClass:[UIWindow class]]) {
                result = ((UIWindow *)vc).rootViewController;
                result = [self findViewController:result];
            }
        }
    }
    return result;
}

@end
