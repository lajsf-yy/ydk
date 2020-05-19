//
//  LLMQTTClient.h
//  MQTT
//
//  Created by sky on 2019/6/21.
//  Copyright © 2019 sky. All rights reserved.
//
#import <ydk-core/YdkCore.h>
#import <Foundation/Foundation.h>
#import "MQTTCFSocketTransport.h"
#import "MQTTSessionManager.h"
#import "MQTTClient.h"
#import "MQTTSession.h"



NS_ASSUME_NONNULL_BEGIN

#define MQTTClientStance [LLMQTTClient sharedInstance]

@protocol YdkMQTTClientProtocol <NSObject>
@optional
- (void)handleMessage:(NSData *)data onTopic:(NSString *)topic retained:(BOOL)retained;
@end


@interface YdkMQTTClient : NSObject<YdkModule>

@property (nonatomic, assign) BOOL isDiscontent;

@property (nonatomic, weak) id <YdkMQTTClientProtocol> interceptor;

@property (nonatomic,strong) MQTTSessionManager *mySessionManager;



/**
  注册mqtt代理
 */
+ (void)registerMqttInterceptor:(id<YdkMQTTClientProtocol>)interceptor;

/**
 连接
 
 @param username 用户名
 @param password 用户密码
 @param cliendId 连接Id
 */
+ (void)bindWithUserName:(NSString *)username password:(NSString *)password cliendId:(NSString *)cliendId connectHandler:(MQTTConnectHandler)connectHandler;

/**
 断开连接
 */
+ (void)disconnect;


/**
 订阅主题
 
 @param topic 主题
 */
+ (void)subscribeTopic:(NSString *)topic;

/**
 取消订阅
 
 @param topic 主题
 */
+ (void)unsubscribeTopic:(NSString *)topic;

/**
 发布消息
 */
+ (void)sendDataToTopic:(NSString *)topic dict:(NSDictionary *)dict;


@end

NS_ASSUME_NONNULL_END
