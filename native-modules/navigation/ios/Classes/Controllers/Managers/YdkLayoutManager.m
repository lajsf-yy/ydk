//
//  YdkLayoutManager.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkLayoutManager.h"
#import "YdkLayoutProtocol.h"
#import "UIViewController+LayoutProtocol.h"

@implementation YdkLayoutManager

+ (UIViewController *)findComponentForId:(NSString *)componentId {
  for (UIWindow* window in UIApplication.sharedApplication.windows) {
    UIViewController* result = [self findChildComponentForParent:window.rootViewController ForId:componentId];
    if (result) {
      return result;
    }
  }
  
  return nil;
}

+ (UIViewController *)findChildComponentForParent:(UIViewController *)parentViewController ForId:(NSString *)componentId {
  if ([parentViewController.layoutInfo.componentId isEqualToString:componentId]) {
    return parentViewController;
  }
  
  if (parentViewController.presentedViewController) {
    if ([parentViewController.presentedViewController.layoutInfo.componentId isEqualToString:componentId]) {
      return parentViewController.presentedViewController;
    }
    
    UIViewController* modalResult = [self findChildComponentForParent:parentViewController.presentedViewController ForId:componentId];
    if (modalResult) {
      return modalResult;
    }
    
  }
  
  for (UIViewController* childVC in parentViewController.childViewControllers) {
    UIViewController* result = [self findChildComponentForParent:childVC ForId:componentId];
    if (result) {
      return result;
    }
  }
  
  return nil;
}

@end
