//
//  LLLayoutNode.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface YdkLayoutNode : NSObject

@property NSString* type;
@property NSString* nodeId;
@property NSDictionary* data;

+(instancetype)create:(NSDictionary *)json;

@end
