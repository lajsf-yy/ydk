//
//  YdkNavigationStackManager.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef void (^YdkTransitionCompletionBlock)(void);
typedef void (^YdkPopCompletionBlock)(NSArray* poppedViewControllers);
typedef void (^YdkTransitionRejectionBlock)(NSString *code, NSString *message, NSError *error);

@interface YdkNavigationStackManager : NSObject

- (void)push:(UIViewController *)newTop onTop:(UIViewController *)onTopViewController animated:(BOOL)animated animationDelegate:(id)animationDelegate completion:(YdkTransitionCompletionBlock)completion rejection:(YdkTransitionRejectionBlock)rejection;

- (void)pop:(UIViewController *)viewController animated:(BOOL)animated completion:(YdkTransitionCompletionBlock)completion rejection:(YdkTransitionRejectionBlock)rejection;

- (void)popTo:(UIViewController *)viewController animated:(BOOL)animated completion:(YdkPopCompletionBlock)completion rejection:(YdkTransitionRejectionBlock)rejection;

- (void)popToRoot:(UIViewController*)viewController animated:(BOOL)animated completion:(YdkPopCompletionBlock)completion rejection:(YdkTransitionRejectionBlock)rejection;

- (void)showModal:(UIViewController *)newTop onTop:(UIViewController *)onTopViewController animated:(BOOL)animated completion:(YdkTransitionCompletionBlock)completion rejection:(YdkTransitionRejectionBlock)rejection;

- (void)dismissModal:(UIViewController*)viewController animated:(BOOL)animated completion:(YdkTransitionCompletionBlock)completion rejection:(YdkTransitionRejectionBlock)rejection;

@end

