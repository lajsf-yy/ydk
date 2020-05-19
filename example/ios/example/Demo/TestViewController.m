//
//  TestViewController.m
//  example
//
//  Created by sky on 2019/6/21.
//  Copyright © 2019 Facebook. All rights reserved.
//

#import "TestViewController.h"
//#import <ydk-mqtt/YdkMQTTClient.h>
@interface TestViewController ()

@end

@implementation TestViewController

- (void)viewDidLoad {
    [super viewDidLoad];
    // Do any additional setup after loading the view from its nib.
//    [YdkMQTTClient registerMqttInterceptor:self];
}
- (IBAction)connect:(id)sender {
//    [YdkMQTTClient bindWithUserName:@"332268828639232" password:@"V7i595jVUu2i1561093863033" cliendId:@"sky" connectHandler:^(NSError *error) {
//
//    }];
}
- (IBAction)subscribe:(id)sender {
//    [YdkMQTTClient subscribeTopic:@"toc/chatRoom/381507955499009"];
    
}
- (IBAction)unsubscribe:(id)sender {
//    [YdkMQTTClient unsubscribeTopic:@"toc/chatRoom/381507955499009"];
    
}

- (IBAction)sendMessage:(id)sender {
    
//    [YdkMQTTClient sendDataToTopic:@"toc/chatRoom/381507955499009" dict:@{}];
    
}



/**
 接收消息
 
 @param data 消息内容
 @param topic 主题
 @param retained 保留
 */
- (void)handleMessage:(NSData *)data onTopic:(NSString *)topic retained:(BOOL)retained{
    
    
    
}


@end
