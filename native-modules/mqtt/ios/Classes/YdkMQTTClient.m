//
//  YdkMQTTClient.m
//  MQTT
//
//  Created by sky on 2019/6/21.
//  Copyright © 2019 sky. All rights reserved.
//


#import "YdkMQTTClient.h"
#import <ydk-network/YdkNetInfo.h>

@interface YdkMQTTConfig : NSObject

@property (nonatomic, copy) NSString *host;
@property (nonatomic, assign) int port;
@property (nonatomic,copy) NSString *username;
@property (nonatomic,copy) NSString *password;
@property (nonatomic,copy) NSString *cliendId;

@end

@implementation YdkMQTTConfig

@end


@interface YdkMQTTClient () <MQTTSessionManagerDelegate>

@property (nonatomic,strong) MQTTCFSocketTransport *myTransport;

@property (nonatomic, strong) YdkMQTTConfig *config;
//订阅的topic
@property (nonatomic,strong) NSMutableDictionary *subedDict;

@end

@implementation YdkMQTTClient

+ (void)load {
    ydk_register_module(self);
}

- (instancetype)initWithConfig:(NSDictionary *)config {
    self = [super init];
    if (self) {
        _config = [YdkMQTTConfig new];
        _config.host =  [config valueForKeyPath:@"mqtt.host"];
        _config.port =  [[config valueForKeyPath:@"mqtt.port"] intValue];
        [self reachabilityChanged];
    }
    return self;
}

+ (void)registerMqttInterceptor:(id<YdkMQTTClientProtocol>)interceptor{
    
    YdkMQTTClient *module = ydk_get_module_instance(YdkMQTTClient.class);
    module.interceptor = interceptor;
}

+ (void)disconnect {
    YdkMQTTClient *module = ydk_get_module_instance(YdkMQTTClient.class);

    module.isDiscontent = YES;
    [module.mySessionManager disconnectWithDisconnectHandler:^(NSError *error) {
        NSLog(@"断开连接  error = %@",[error description]);
    }];
    [module.mySessionManager setDelegate:nil];
    module.mySessionManager = nil;
    
}


+ (void)reConnect {
    YdkMQTTClient *module = ydk_get_module_instance(YdkMQTTClient.class);

    if (module.mySessionManager && module.mySessionManager.port) {
        module.mySessionManager.delegate = module;
        module.isDiscontent = NO;
        [module.mySessionManager connectToLast:^(NSError *error) {
            NSLog(@"重新连接  error = %@",[error description]);
        }];
        module.mySessionManager.subscriptions = module.subedDict;
        
    }
    else {
        [YdkMQTTClient bindWithUserName:module.config.username password:module.config.password cliendId:module.config.cliendId connectHandler:^(NSError *error) {
        }];
        
    }
    
}

- (void)reachabilityChanged{
    [YdkNetInfo addNetStatusChangeObserver:^(YdkNetStatus netStatus) {
            if (netStatus == YdkNetStatusNone) {
                [YdkMQTTClient disconnect];
            }
            else if (self.mySessionManager.state != MQTTSessionManagerStateConnected) {
                [YdkMQTTClient reConnect];
            }
    }];
}

#pragma mark - 绑定
+ (void)bindWithUserName:(NSString *)username password:(NSString *)password cliendId:(NSString *)cliendId connectHandler:(MQTTConnectHandler)connectHandler{
    YdkMQTTClient *module = ydk_get_module_instance(YdkMQTTClient.class);
    module.config.username = username;
    module.config.password = password;
    module.config.cliendId = cliendId;
    
    [module.mySessionManager connectTo:module.config.host
                                port:module.config.port
                                 tls:NO
                           keepalive:60
                               clean:YES
                                auth:YES
                                user:module.config.username
                                pass:module.config.password
                                will:NO
                           willTopic:nil
                             willMsg:nil
                             willQos:MQTTQosLevelAtLeastOnce
                      willRetainFlag:NO
                        withClientId:module.config.cliendId
                      securityPolicy:[module customSecurityPolicy]
                        certificates:nil
                       protocolLevel:4
                      connectHandler:^(NSError *error) {
                          MQTTSessionError sessionError = error.code;
                          if (sessionError == MQTTSessionErrorConnackNotAuthorized) {  //鉴权失败
                              connectHandler(error);
                          }
                      }];
    
    module.isDiscontent = NO;
    module.mySessionManager.subscriptions = module.subedDict;
    
}

- (MQTTSSLSecurityPolicy *)customSecurityPolicy
{
    
    MQTTSSLSecurityPolicy *securityPolicy = [MQTTSSLSecurityPolicy policyWithPinningMode:MQTTSSLPinningModeNone];
    
    securityPolicy.allowInvalidCertificates = YES;
    securityPolicy.validatesCertificateChain = YES;
    securityPolicy.validatesDomainName = NO;
    return securityPolicy;
}


#pragma mark ---- 状态
- (void)sessionManager:(MQTTSessionManager *)sessionManager didChangeState:(MQTTSessionManagerState)newState {
    
    switch (newState) {
        case MQTTSessionManagerStateConnected:
            NSLog(@"eventCode -- 连接成功");
            break;
        case MQTTSessionManagerStateConnecting:
            NSLog(@"eventCode -- 连接中");
            break;
        case MQTTSessionManagerStateClosed:
            NSLog(@"eventCode -- 连接被关闭");
            break;
        case MQTTSessionManagerStateError:
            NSLog(@"eventCode -- 连接错误");
            break;
        case MQTTSessionManagerStateClosing:
            NSLog(@"eventCode -- 关闭中");
            
            break;
        case MQTTSessionManagerStateStarting:
            NSLog(@"eventCode -- 连接开始");
            
            break;
            
        default:
            break;
    }
     
}


#pragma mark MQTTSessionManagerDelegate
- (void)handleMessage:(NSData *)data onTopic:(NSString *)topic retained:(BOOL)retained {
    
    if (_interceptor && [_interceptor respondsToSelector:@selector(handleMessage:onTopic:retained:)]) {
        [_interceptor handleMessage:data onTopic:topic retained:retained];
    }
}


#pragma mark - 订阅
+ (void)subscribeTopic:(NSString *)topic{
    
    YdkMQTTClient *module = ydk_get_module_instance(YdkMQTTClient.class);
    if (![module.subedDict.allKeys containsObject:topic]) {
        [module.subedDict setObject:[NSNumber numberWithLong:MQTTQosLevelAtLeastOnce] forKey:topic];
        NSLog(@"订阅字典 ----------- = %@",module.subedDict);
        module.mySessionManager.subscriptions =  module.subedDict;
    }
    else {
        NSLog(@"已经存在，不用订阅");
    }
    
}

#pragma mark - 取消订阅
+ (void)unsubscribeTopic:(NSString *)topic {
    
    YdkMQTTClient *module = ydk_get_module_instance(YdkMQTTClient.class);
    if ([module.subedDict.allKeys containsObject:topic]) {
        [module.subedDict removeObjectForKey:topic];
        NSLog(@"更新之后的订阅字典 ----------- = %@",module.subedDict);
        module.mySessionManager.subscriptions =  module.subedDict;
    }
    else {
        NSLog(@"不存在，无需取消");
    }
    
}

#pragma mark - 发布消息
+ (void)sendDataToTopic:(NSString *)topic dict:(NSDictionary *)dict {
    
    YdkMQTTClient *module = ydk_get_module_instance(YdkMQTTClient.class);
    NSData *data = [NSJSONSerialization dataWithJSONObject:dict options:0 error:nil];
    [module.mySessionManager sendData:data topic:topic qos:MQTTQosLevelAtLeastOnce retain:NO];
}

#pragma mark - 懒加载
- (MQTTSessionManager *)mySessionManager {
    if (!_mySessionManager) {
        _mySessionManager = [[MQTTSessionManager alloc]init];
        _mySessionManager.delegate = self;
    }
    return _mySessionManager;
}

- (MQTTCFSocketTransport *)myTransport {
    if (!_myTransport) {
        _myTransport = [[MQTTCFSocketTransport alloc]init];
        _myTransport.host = _config.host;
        _myTransport.port = _config.port;
        _myTransport.tls = NO;
    }
    return _myTransport;
}

- (NSMutableDictionary *)subedDict {
    if (!_subedDict) {
        _subedDict = [NSMutableDictionary dictionary];
    }
    return _subedDict;
}



@end
