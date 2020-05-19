//
//  YdkNavigationOptions.m
//  NutritionPlan
//
//  Created by yryz on 2019/11/15.
//  Copyright © 2019 laj. All rights reserved.
//

#import "YdkNavigationOptions.h"

@implementation YdkNavigationOptions

- (instancetype)init {
    if (self = [super init]) {
        _popGesture = YES;
    }
    return self;
}

- (instancetype)initWithDict:(NSDictionary *)dict {
    if (self = [self init]) {
        if ([dict objectForKey:@"popGesture"]) self.popGesture = [[dict objectForKey:@"popGesture"] boolValue];
        return self;
    }
    return self;
}

@end
