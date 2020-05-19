//
//  YdkLocation.h
//  ydk-location
//
//  Created by yryz on 2019/7/25.
//

#import <Foundation/Foundation.h>

#import "YdkLocationProtocol.h"

@interface YdkLocation : NSObject <YdkLocationProtocol>

+ (RACSignal<YdkLocationInfo *> *)requestLocation;

@end
