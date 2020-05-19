//
//  YdkNavigationOptions.h
//  NutritionPlan
//
//  Created by yryz on 2019/11/15.
//  Copyright © 2019 laj. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface YdkNavigationOptions : NSObject

@property (nonatomic, assign) BOOL popGesture;

- (instancetype)initWithDict:(NSDictionary *)dict;

@end

