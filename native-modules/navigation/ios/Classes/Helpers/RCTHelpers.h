//
//  RCTHelpers.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTRootView.h>

@interface RCTHelpers : NSObject
+ (NSMutableDictionary *)textAttributesFromDictionary:(NSDictionary *)dictionary withPrefix:(NSString *)prefix;
+ (NSMutableDictionary *)textAttributesFromDictionary:(NSDictionary *)dictionary withPrefix:(NSString *)prefix baseFont:(UIFont *)font;
+ (NSString*)getTimestampString;
+ (BOOL)removeYellowBox:(RCTRootView *)reactRootView;
@end
