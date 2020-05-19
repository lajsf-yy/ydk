//
//  YdkReactRootViewCreator.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "YdkRootViewCreator.h"
#import <React/RCTBridge.h>

@interface YdkReactRootViewCreator : NSObject <YdkRootViewCreator>

-(instancetype)initWithBridge:(RCTBridge*)bridge;

@end
