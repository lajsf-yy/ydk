#import <ydk-core/YdkCore.h>

#import "YdkNetworkProtocol.h"
#import "YdkDownloadInfo.h"
#import "YdkUploadInfo.h"

FOUNDATION_EXPORT NSErrorUserInfoKey const YdkNSURLSessionDataTaskKey;
FOUNDATION_EXPORT NSErrorUserInfoKey const YdkNetworkResponseObjectKey;

@protocol YdkNetworkInterceptorProtocol <NSObject>

@optional

/**
 网络请求处理异常code码
 
 @param errCode 异常code码
 @return 处理结果
 */
- (RACSignal<id> *)handleErrorResponse:(NSInteger)errCode;

/**
 网络请求处理异常code码
 
 @param errCode 异常code码
 @param responseObject 请求响应数据
 @return 处理结果
 */
- (RACSignal<id> *)handleErrorResponse:(NSInteger)errCode responseObject:(id)responseObject;

/**
 网络请求前请求体的自定义
 
 @param request 原始请求对象
 @return 加工后的请求对象
 */
- (YdkRequest *)requestWithIncompleteRequest:(YdkRequest *)request;

/**
 网络请求error异常处理
 
 @param error 错误
 @return 自定义的error信息
 */
- (NSError *)responseError:(NSError *)error;

@end

@class YdkDownloadInfo, YdkUploadInfo;
@interface YdkNetwork: NSObject <YdkModule, YdkNetworkProtocol>

// MARK: - Debug Mode
+ (void)startDebugMode;
+ (void)stopDebugMode;

// MARK: - 注册网络请求前、后处理对象
+ (void)registerNetworkInterceptor:(id<YdkNetworkInterceptorProtocol>)interceptor;

// MARK: - HTTP Service
+ (RACSignal<id> *)GET:(NSString *)URLString parameters:(id)parameters;
+ (RACSignal<id> *)GET:(NSString *)service URLString:(NSString *)URLString parameters:(id)parameters;

+ (RACSignal<id> *)POST:(NSString *)URLString parameters:(id)parameters;
+ (RACSignal<id> *)POST:(NSString *)service URLString:(NSString *)URLString parameters:(id)parameters;

+ (RACSignal<id> *)DELETE:(NSString *)URLString parameters:(id)parameters;
+ (RACSignal<id> *)DELETE:(NSString *)service URLString:(NSString *)URLString parameters:(id)parameters;

+ (RACSignal<id> *)PUT:(NSString *)URLString parameters:(id)parameters;
+ (RACSignal<id> *)PUT:(NSString *)service URLString:(NSString *)URLString parameters:(id)parameters;

/**
 HTTP 网络请求

 @param method 请求方法
 @param URLString 请求地址：path or 完整url
 @param parameters 请求参数
 @return 响应数据
 */
+ (RACSignal<id> *)request:(YdkHTTPMethod)method URLString:(NSString *)URLString parameters:(id)parameters;


/**
 HTTP 网络请求

 @param method 请求方法
 @param service 用于指定拼接请求地址时host后面的服务名称
 @param URLString 请求地址：默认在其前面拼接host、服务名称service、apiVersion[由env中读取]
 @param parameters 请求参数
 @return 响应数据
 */
+ (RACSignal<id> *)request:(YdkHTTPMethod)method service:(NSString *)service URLString:(NSString *)URLString parameters:(id)parameters;

/**
 HTTP 下载
 
 @param sourceURL 远程文件URL eg: https://cdn.yryz.com/yryz-new/image/3C2E31BF-C7E0-4DB8-A225-0420DF37A642.jpg
 @return 下载信息
 */
+ (RACSignal<YdkDownloadInfo *> *)downloadWithSourceURL:(NSURL *)sourceURL;

/**
 HTTP 下载
 
 @param sourceURL 远程文件URL eg: https://cdn.yryz.com/yryz-new/image/3C2E31BF-C7E0-4DB8-A225-0420DF37A642.jpg
 @param targetURL 目标目录URL，未传默认放在沙盒Cache文件下 eg: ../cache/
 @return 下载信息
 */
+ (RACSignal<YdkDownloadInfo *> *)downloadWithSourceURL:(NSURL *)sourceURL targetURL:(NSURL *)targetURL;

// MARK: - OSS Upload Service
/**
 上传图片
 
 @param uploadData 上传图片数据
 @return 上传结果
 */
+ (RACSignal<YdkUploadInfo *> *)uploadWithData:(NSData *)uploadData;

/**
 上传音视频
 
 @param fileURL 文件地址
 @return 上传结果
 */
+ (RACSignal<YdkUploadInfo *> *)uploadWithFileURL:(NSURL *)fileURL;

/**
 上传图片
 
 @param uploadData 上传图片数据
 @param fileType 上传数据类型，用于将上传资源存储于哪个目录下 head（头像），image（图片），audio（音频），video（视频）
 @return 上传结果
 */
+ (RACSignal<YdkUploadInfo *> *)uploadWithData:(NSData *)uploadData fileType:(NSString *)fileType;

/**
 上传文件
 
 @param fileURL 文件地址
 @param fileType 上传数据类型，用于将上传资源存储于哪个目录下 head（头像），image（图片），audio（音频），video（视频）
 @return 上传结果
 */
+ (RACSignal<YdkUploadInfo *> *)uploadWithFileURL:(NSURL *)fileURL fileType:(NSString *)fileType;


/**
 工具方法: 根据请求参数，返回请求对象

 @param method 请求方法
 @param URLString 请求地址
 @param parameters 请求参数
 @return 请求对象
 */
- (YdkRequest *)requestWithMethod:(YdkHTTPMethod)method URLString:(NSString *)URLString parameters:(id)parameters;

@end
