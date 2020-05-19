//
//  YdkNavigationStackManager.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkNavigationStackManager.h"
#import "YdkErrorHandler.h"
#import <React/RCTI18nUtil.h>

typedef void (^YdkAnimationBlock)(void);

@implementation YdkNavigationStackManager

- (void)push:(UIViewController *)newTop onTop:(UIViewController *)onTopViewController animated:(BOOL)animated animationDelegate:(id)animationDelegate completion:(YdkTransitionCompletionBlock)completion rejection:(RCTPromiseRejectBlock)rejection {
  UINavigationController *nvc = onTopViewController.navigationController;
  
  if([[RCTI18nUtil sharedInstance] isRTL]) {
    nvc.view.semanticContentAttribute = UISemanticContentAttributeForceRightToLeft;
    nvc.navigationBar.semanticContentAttribute = UISemanticContentAttributeForceRightToLeft;
  } else {
    nvc.view.semanticContentAttribute = UISemanticContentAttributeForceLeftToRight;
    nvc.navigationBar.semanticContentAttribute = UISemanticContentAttributeForceLeftToRight;
  }
  
  if (animationDelegate) {
    nvc.delegate = animationDelegate;
  } else {
    nvc.delegate = nil;
  }
  
  [self performAnimationBlock:^{
    [nvc pushViewController:newTop animated:animated];
  } completion:completion];
}

- (void)pop:(UIViewController *)viewController animated:(BOOL)animated completion:(YdkTransitionCompletionBlock)completion rejection:(YdkTransitionRejectionBlock)rejection {
  if (!viewController.view.window) {
    animated = NO;
  }
  
  __block UIViewController *poppedVC = nil;
  [self performAnimationBlock:^{
    poppedVC = [viewController.navigationController popViewControllerAnimated:animated];
  } completion:^{
    if (poppedVC) {
      completion();
    } else {
      [YdkErrorHandler reject:rejection withErrorCode:1012 errorDescription:@"popping component failed"];
    }
  }];
}

- (void)popTo:(UIViewController *)viewController animated:(BOOL)animated completion:(YdkPopCompletionBlock)completion rejection:(YdkTransitionRejectionBlock)rejection; {
  __block NSArray* poppedVCs;
  
  if ([viewController.navigationController.childViewControllers containsObject:viewController]) {
    [self performAnimationBlock:^{
      poppedVCs = [viewController.navigationController popToViewController:viewController animated:animated];
    } completion:^{
      if (completion) {
        completion(poppedVCs);
      }
    }];
  } else {
    [YdkErrorHandler reject:rejection withErrorCode:1011 errorDescription:@"component not found in stack"];
  }
}

- (void)popToRoot:(UIViewController*)viewController animated:(BOOL)animated completion:(YdkPopCompletionBlock)completion rejection:(YdkTransitionRejectionBlock)rejection {
  __block NSArray* poppedVCs;
  
  [self performAnimationBlock:^{
    poppedVCs = [viewController.navigationController popToRootViewControllerAnimated:animated];
  } completion:^{
    completion(poppedVCs);
  }];
}

- (void)showModal:(UIViewController *)newTop onTop:(UIViewController *)onTopViewController animated:(BOOL)animated completion:(YdkTransitionCompletionBlock)completion rejection:(YdkTransitionRejectionBlock)rejection {
    UINavigationController *topNC = onTopViewController.navigationController;
    UINavigationController *newTopNC = [[UINavigationController alloc] initWithRootViewController:newTop];
    newTopNC.hidesBottomBarWhenPushed = YES;
    newTopNC.navigationBar.hidden = YES;
    
    [self performAnimationBlock:^{
        newTopNC.modalPresentationStyle = UIModalPresentationFullScreen;
        [topNC presentViewController:newTopNC animated:animated completion:nil];
    } completion:completion];
}

- (void)dismissModal:(UIViewController*)viewController animated:(BOOL)animated completion:(YdkTransitionCompletionBlock)completion rejection:(YdkTransitionRejectionBlock)rejection {
    if (!viewController.view.window) {
        animated = NO;
    }
    
    [self performAnimationBlock:^{
        [viewController.navigationController dismissViewControllerAnimated:animated completion:nil];
    } completion:completion];
}

- (void)setStackChildren:(NSArray<UIViewController *> *)children fromViewController:(UIViewController *)fromViewController animated:(BOOL)animated completion:(YdkTransitionCompletionBlock)completion rejection:(YdkTransitionRejectionBlock)rejection {
  UINavigationController* nvc = fromViewController.navigationController;
  
  [self performAnimationBlock:^{
    [nvc setViewControllers:children animated:animated];
  } completion:completion];
}

# pragma mark Private

- (void)performAnimationBlock:(YdkAnimationBlock)animationBlock completion:(YdkTransitionCompletionBlock)completion {
  [CATransaction begin];
  [CATransaction setCompletionBlock:^{
    if (completion) {
      completion();
    }
  }];
  
  animationBlock();
  
  [CATransaction commit];
}

@end
