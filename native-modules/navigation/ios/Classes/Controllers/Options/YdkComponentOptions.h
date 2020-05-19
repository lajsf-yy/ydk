//
//  RNNComponentOptions.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface YdkComponentOptions : NSObject

@property (nonatomic, copy) NSString *name;
@property (nonatomic, copy) NSString *componentId;

- (instancetype)initWithDict:(NSDictionary*)dict;

@end
