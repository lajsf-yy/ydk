//
//  LLLayoutNode.m
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import "YdkLayoutNode.h"

@implementation YdkLayoutNode

+(instancetype)create:(NSDictionary *)json
{
  YdkLayoutNode* node = [YdkLayoutNode new];
  node.type = json[@"type"];
  node.nodeId = json[@"id"];
  node.data = json[@"data"];
  return node;
}

@end
