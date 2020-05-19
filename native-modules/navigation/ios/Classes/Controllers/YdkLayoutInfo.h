//
//  LLLayoutInfo.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "YdkLayoutNode.h"

@interface YdkLayoutInfo : NSObject

- (instancetype)initWithNode:(YdkLayoutNode *)node;

@property (nonatomic, strong) NSString* componentId;
@property (nonatomic, strong) NSString* name;
@property (nonatomic, strong) NSDictionary* props;

@end
