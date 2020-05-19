//
//  UIDevice+toolkit.h
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import <UIKit/UIKit.h>

@interface UIDevice (toolkit)

@property (class, readonly, nonatomic, copy) NSString *APPVersion; // e.g. @"1.0.0"
@property (class, readonly, nonatomic, copy) NSString *OSVersion;  // e.g. @"10.3.3"
@property (class, readonly, nonatomic, copy) NSString *deviceID;   // e.g. @"10.3.3"
@property (class, readonly, nonatomic, copy) NSString *deviceName; // e.g. @"iPhone6"
@property (class, readonly, nonatomic, copy) NSString *IP;         // e.g. @"192.168.1.111"

@end

