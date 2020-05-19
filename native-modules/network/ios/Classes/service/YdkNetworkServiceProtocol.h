//
//  YdkNetworkProtocol.h
//  ydk
//
//  Created by yryz on 2019/6/18.
//

#import <Foundation/Foundation.h>
#import <ReactiveObjC/ReactiveObjC.h>
#import "YdkRequest.h"

FOUNDATION_EXPORT NSErrorUserInfoKey const YdkNSURLSessionDataTaskKey;
FOUNDATION_EXPORT NSErrorUserInfoKey const YdkNetworkResponseObjectKey;

FOUNDATION_EXPORT NSErrorDomain const YdkNetworkErrorDomain;
NS_ERROR_ENUM(YdkNetworkErrorDomain)
{
    YdkNetworkErrorResponseDataInvalid          = -1000,     // 接口响应数据格式错误
    YdkNetworkErrorDownloadFailed               = -1001,     // 下载失败
    YdkNetworkErrorInvalidSourceURL             = -1002,     // 无效的文件URL
    YdkNetworkErrorUnableHandleErrorResponse    = -1003,     // 无法处理响应数据异常，请调用[YdkNetwork registerNetworkInterceptor:] 方法注册拦截器
    
    YdkNetworkErrorLackOSSParams                = -2001,     // 缺少oss上传参数
    YdkNetworkErrorInvalidFileURL               = -2002,     // 无效的文件URL
    YdkNetworkErrorInvalidUploadData            = -2003,     // 无效的上传NSData
    YdkNetworkErrorUploadFailed                 = -2004,     // 上传失败
};

@class YdkDownloadInfo;
@protocol YdkHTTPServiceProtocol <NSObject>

- (RACSignal<id> *)request:(YdkRequest *)request;

- (RACSignal<YdkDownloadInfo *> *)downloadWithSourceURL:(NSURL *)sourceURL targetURL:(NSURL *)targetURL;

@end

@class YdkUploadInfo;
@protocol YdkOSSUploadServiceProtocol <NSObject>

- (RACSignal<YdkUploadInfo *> *)uploadWithData:(NSData *)uploadData fileType:(NSString *)fileType;
- (RACSignal<YdkUploadInfo *> *)uploadWithFileURL:(NSURL *)fileURL fileType:(NSString *)fileType;

@end
