//
//  NSData+toolkit.m
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import "NSData+toolkit.h"

@implementation NSData (toolkit)

- (NSString *)tk_imageFileSuffix {
    uint8_t c;
    [self getBytes:&c length:1];
    switch (c) {
            case 0x47:
            return @"gif";
            break;
        default:
            return @"jpg";
            break;
    }
}

@end
