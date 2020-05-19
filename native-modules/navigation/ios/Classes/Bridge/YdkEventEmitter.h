//
//  YdkEventEmitter.h
//  LoveLorn
//
//  Created by yryz on 2019/7/1.
//  Copyright © 2019 yryz. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <React/RCTEventEmitter.h>

@interface YdkEventEmitter : RCTEventEmitter <RCTBridgeModule>

- (void)sendComponentDidAppear:(NSString*)componentId componentName:(NSString*)componentName;

- (void)sendComponentDidDisappear:(NSString*)componentId componentName:(NSString*)componentName;

- (void)sendComponentReceiveResult:(NSString *)componentId data:(id)data;

@end

