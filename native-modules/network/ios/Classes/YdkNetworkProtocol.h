//
//  YdkNetworkProtocol.h
//  ydk-network
//
//  Created by yryz on 2019/7/12.
//

#import <Foundation/Foundation.h>
#import <ReactiveObjC/ReactiveObjC.h>

#import "YdkRequest.h"

@protocol YdkNetworkProtocol <NSObject>

/**
 HTTP 网络请求
 
 @param method 请求方法
 @param URLString 请求地址：path or 完整url
 @param parameters 请求参数
 @return 响应数据
 */
- (RACSignal<id> *)request:(YdkHTTPMethod)method URLString:(NSString *)URLString parameters:(id)parameters;


/**
 HTTP 网络请求
 
 @param method 请求方法
 @param service 用于指定拼接请求地址时host后面的服务名称
 @param URLString 请求地址：默认在其前面拼接host、服务名称service、apiVersion[由env中读取]
 @param parameters 请求参数
 @return 响应数据
 */
- (RACSignal<id> *)request:(YdkHTTPMethod)method service:(NSString *)service URLString:(NSString *)URLString parameters:(id)parameters;


/**
 拦截网络请求响应处理

 @param responseObject 原始响应对象
 @param request 原始请求
 @return 拦截后处理返回数据
 */
- (RACSignal<id> *)interceptorsResponse:(id)responseObject request:(YdkRequest *)request;

@end

