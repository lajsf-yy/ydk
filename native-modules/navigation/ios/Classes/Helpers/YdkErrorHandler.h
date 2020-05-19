//
//  YdkErrorHandler.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTBridgeModule.h>

typedef enum YdkCommandsErrorCodes {
  YdkCommandErrorCodeNoStack = 0
} YdkCommandsErrorCodes;

@interface YdkErrorHandler : NSObject

+ (void)reject:(RCTPromiseRejectBlock)reject withErrorCode:(NSInteger)errorCode errorDescription:(NSString*)errorDescription;
+ (NSString *)getCallerFunctionName;

@end
