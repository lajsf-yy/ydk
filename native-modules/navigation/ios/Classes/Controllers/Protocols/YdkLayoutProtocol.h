//
//  LLLayoutProtocol.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "YdkLayoutInfo.h"
#import "YdkRootViewCreator.h"
#import "YdkEventEmitter.h"
#import "YdkNavigationOptions.h"

@protocol YdkLayoutProtocol <NSObject, UINavigationControllerDelegate, UIViewControllerTransitioningDelegate>

@property (nonatomic, strong) YdkNavigationOptions* options;

@required

- (instancetype)initWithLayoutInfo:(YdkLayoutInfo *)layoutInfo
                           creator:(id<YdkRootViewCreator>)creator
                      eventEmitter:(YdkEventEmitter *)eventEmitter;

- (void)mergeOptions:(YdkNavigationOptions *)options;

@end
