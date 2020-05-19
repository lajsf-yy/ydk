//
//  LLRootViewController.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "YdkLayoutNode.h"
#import "YdkRootViewCreator.h"
#import "YdkEventEmitter.h"
#import "YdkLayoutInfo.h"
#import "YdkLayoutProtocol.h"
#import "YdkNavigationResultProtocol.h"
#import "UIViewController+LayoutProtocol.h"

@interface YdkRootViewController : UIViewController <YdkLayoutProtocol, YdkNavigationResultProtocol, UINavigationControllerDelegate>

@property (readonly, nonatomic, assign) BOOL renderReady;

@property (nonatomic, strong) YdkEventEmitter *eventEmitter;
@property (nonatomic, retain) YdkLayoutInfo* layoutInfo;

@end
