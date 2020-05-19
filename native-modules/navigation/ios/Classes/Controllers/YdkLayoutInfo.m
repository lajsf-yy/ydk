//
//  LLLayoutInfo.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkLayoutInfo.h"

@implementation YdkLayoutInfo

- (instancetype)initWithNode:(YdkLayoutNode *)node {
  self = [super init];
  
  self.componentId = node.nodeId;
  self.name = node.data[@"name"];
  self.props = node.data[@"passProps"];
  
  return self;
}

@end
