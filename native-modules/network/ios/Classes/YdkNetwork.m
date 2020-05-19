#import "YdkNetwork.h"
#import "YdkNetworkServiceProtocol.h"
#import "YdkHTTPSerivce.h"
#import "YdkOssUploadService.h"

NSErrorUserInfoKey const YdkNSURLSessionDataTaskKey = @"YdkNSURLSessionDataTaskKey";
NSErrorUserInfoKey const YdkNetworkResponseObjectKey = @"YdkNetworkResponseObjectKey";

NSErrorDomain const YdkNetworkErrorDomain = @"YdkNetworkErrorDomain";

struct YdkNetworkInterceptor {
    unsigned int handleError                : 1;
    unsigned int handleErrorResponseObject  : 1;
    unsigned int requestCustom              : 1;
    unsigned int responseError              : 1;
} _interceptorFlags;

@interface YdkNetworkConfig : NSObject

@property (nonatomic, copy) NSString *ossAccessKeyId;
@property (nonatomic, copy) NSString *ossAccessKey;
@property (nonatomic, copy) NSString *ossBucketName;
@property (nonatomic, copy) NSString *ossHost;

@property (nonatomic, copy) NSString *name;
@property (nonatomic, copy) NSString *baseUrl;
@property (nonatomic, copy) NSString *apiVersion;

@end

@implementation YdkNetworkConfig

@end

@interface YdkNetwork ()

@property (nonatomic, strong) id<YdkNetworkInterceptorProtocol> interceptor;
@property (nonatomic, strong) YdkNetworkConfig *config;

@property (nonatomic, strong) id<YdkHTTPServiceProtocol> httpService;
@property (nonatomic, strong) id<YdkOSSUploadServiceProtocol> uploadService;

@property (nonatomic, assign) BOOL debugMode;

@end

@implementation YdkNetwork

+ (void)load {
  ydk_register_module(self);
}

- (instancetype)initWithConfig:(NSDictionary *)config {
  self = [super init];
  if (self) {
      _config = [YdkNetworkConfig new];
      _config.ossAccessKeyId = [config valueForKeyPath:@"oss.accessKeyId"];
      _config.ossAccessKey = [config valueForKeyPath:@"oss.secretAccessKey"];
      _config.ossBucketName = [config valueForKeyPath:@"oss.bucketName"];
      _config.ossHost = [config valueForKeyPath:@"oss.cdn"];
      _config.name = [config objectForKey:@"name"];
      _config.baseUrl = [config objectForKey:@"httpBaseUrl"];
      _config.apiVersion = [config objectForKey:@"apiVersion"];
      
      [self _setup];
  }
  return self;
}

- (void)_setup {
    _httpService = [[YdkHTTPSerivce alloc] init];
    _uploadService = [[YdkOssUploadService alloc] initWithAccessKeyId:_config.ossAccessKeyId secretAccessKey:_config.ossAccessKey bucketName:_config.ossBucketName directoryName:_config.name ossHost:_config.ossHost];
}

- (void)setInterceptor:(id<YdkNetworkInterceptorProtocol>)interceptor {
    _interceptor = interceptor;
    
    _interceptorFlags.handleError = [interceptor respondsToSelector:@selector(handleErrorResponse:)];
    _interceptorFlags.handleErrorResponseObject = [interceptor respondsToSelector:@selector(handleErrorResponse:responseObject:)];
    _interceptorFlags.requestCustom = [interceptor respondsToSelector:@selector(requestWithIncompleteRequest:)];
    _interceptorFlags.responseError = [interceptor respondsToSelector:@selector(responseError:)];
}

// MARK: - Protocol

// MARK: - Debug Mode
+ (void)startDebugMode {
#ifdef DEBUG
    YdkNetwork *module = ydk_get_module_instance(YdkNetwork.class);
    module.debugMode = YES;
#endif
}

+ (void)stopDebugMode {
    YdkNetwork *module = ydk_get_module_instance(YdkNetwork.class);
    module.debugMode = NO;
}

// MARK: - 注册网络请求前、后处理对象
+ (void)registerNetworkInterceptor:(id<YdkNetworkInterceptorProtocol>)interceptor {
    YdkNetwork *module = ydk_get_module_instance(YdkNetwork.class);
    module.interceptor = interceptor;
}

// MARK: - HTTP Service
+ (RACSignal<id> *)GET:(NSString *)URLString parameters:(id)parameters {
    return [self request:GET URLString:URLString parameters:parameters];
}

+ (RACSignal<id> *)GET:(NSString *)service URLString:(NSString *)URLString parameters:(id)parameters {
    return [self request:GET service:service URLString:URLString parameters:parameters];
}

+ (RACSignal<id> *)POST:(NSString *)URLString parameters:(id)parameters {
    return [self request:POST URLString:URLString parameters:parameters];
}

+ (RACSignal<id> *)POST:(NSString *)service URLString:(NSString *)URLString parameters:(id)parameters {
    return [self request:POST service:service URLString:URLString parameters:parameters];
}

+ (RACSignal<id> *)DELETE:(NSString *)URLString parameters:(id)parameters {
    return [self request:DELETE URLString:URLString parameters:parameters];
}

+ (RACSignal<id> *)DELETE:(NSString *)service URLString:(NSString *)URLString parameters:(id)parameters {
    return [self request:DELETE service:service URLString:URLString parameters:parameters];
}

+ (RACSignal<id> *)PUT:(NSString *)URLString parameters:(id)parameters {
    return [self request:PUT URLString:URLString parameters:parameters];
}

+ (RACSignal<id> *)PUT:(NSString *)service URLString:(NSString *)URLString parameters:(id)parameters {
    return [self request:PUT service:service URLString:URLString parameters:parameters];
}

+ (RACSignal<id> *)request:(YdkHTTPMethod)method URLString:(NSString *)URLString parameters:(id)parameters {
    return [self _request:method URLString:URLString parameters:parameters];
}

+ (RACSignal<id> *)request:(YdkHTTPMethod)method service:(NSString *)service URLString:(NSString *)URLString parameters:(id)parameters {
    YdkNetwork *module = ydk_get_module_instance(YdkNetwork.class);
    // 拼接 service/version/url
    URLString = [NSString stringWithFormat:@"/%@/%@%@", service, module.config.apiVersion, URLString];
    return [self _request:method URLString:URLString parameters:parameters];
}

+ (RACSignal<id> *)_request:(YdkHTTPMethod)method URLString:(NSString *)URLString parameters:(id)parameters {
    YdkNetwork *module = ydk_get_module_instance(YdkNetwork.class);
    YdkRequest *request = [module requestWithMethod:method URLString:URLString parameters:parameters];
    @weakify(module);
    return [[[module _request:request] flattenMap:^RACSignal *(id value) {
        @strongify(module);
        return [module interceptorsResponse:value request:request];
    }] map:^id (id responseObject) {
        // 只返回data中的数据
        id data = [responseObject objectForKey:@"data"];
        if ([data isEqual:[NSNull null]]) {
            data = nil;
        }
        return data;
    }];
}

// MARK: - Instanse Method

- (RACSignal<id> *)request:(YdkHTTPMethod)method URLString:(NSString *)URLString parameters:(id)parameters {
    return [self _request:method URLString:URLString parameters:parameters];
}
- (RACSignal<id> *)request:(YdkHTTPMethod)method service:(NSString *)service URLString:(NSString *)URLString parameters:(id)parameters {
    URLString = [NSString stringWithFormat:@"/%@/%@%@", service, self.config.apiVersion, URLString];
    return [self _request:method URLString:URLString parameters:parameters];
}

- (RACSignal<id> *)_request:(YdkHTTPMethod)method URLString:(NSString *)URLString parameters:(id)parameters {
    return [self _request:[self requestWithMethod:method URLString:URLString parameters:parameters]];
}

- (RACSignal<id> *)_request:(YdkRequest *)request {
    if (_interceptorFlags.requestCustom) {
        request = [[_interceptor requestWithIncompleteRequest:request] copy];
    }
    @weakify(self);
    return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
        @strongify(self);
        [self logMessage:[NSString stringWithFormat:@"|------------------ BEGIN ------------------|%@\n|------------------- END -------------------|", request]];
        // STEP1: 拿到响应数据
        [[self.httpService request:request] subscribeNext:^(id x) {
            [subscriber sendNext:x];
            [subscriber sendCompleted];
        } error:^(NSError *error) {
            // 401
            NSURLSessionDataTask *task = [error.userInfo objectForKey:YdkNSURLSessionDataTaskKey];
            NSInteger code = ((NSHTTPURLResponse *)task.response).statusCode;
            if (code == 401) {
                // 拿到httpStatusCode为401下的响应数据中的code值
                [subscriber sendNext:@{@"code" : @(102)}];
                [subscriber sendCompleted];
            } else {
                if (_interceptorFlags.responseError) {
                    error = [_interceptor responseError:error];
                }
                [subscriber sendError:error];
            }
        }];
        return nil;
    }];
}

- (RACSignal<id> *)interceptorsResponse:(id)responseObject request:(YdkRequest *)request {
    @weakify(self);
    return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
        @strongify(self);
        
        @weakify(self);
        [[[[[self parseResponse:responseObject] flattenMap:^RACSignal *(id value) {
            @strongify(self);
            NSInteger code = [value integerValue];
            // 提前结束逻辑处理
            if (code == 200) {
                [subscriber sendNext:responseObject];
                [subscriber sendCompleted];
                return nil;
            } else {
                return [self handleErrorResponse:code responseObject:responseObject];
            }
        }] flattenMap:^RACSignal *(id value) {
            @strongify(self);
            // 异常code处理完成后，重新请求一次
            return [self _request:request];
        }] flattenMap:^RACSignal *(id responseValue) {
            @strongify(self);
            return [self interceptorsResponse:responseValue request:request];
        }] subscribeNext:^(id x) {
            [subscriber sendNext:x];
            [subscriber sendCompleted];
        } error:^(NSError *error) {
            if (_interceptorFlags.responseError) {
                @strongify(self);
                error = [self.interceptor responseError:error];
            }
            [subscriber sendError:error];
        }];
        return nil;
    }];
}

- (RACSignal<id> *)parseResponse:(id)responseObject {
    return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
        if ([responseObject isKindOfClass:[NSDictionary class]]) {
            NSInteger code = [[responseObject objectForKey:@"code"] integerValue];
            [subscriber sendNext:@(code)];
            [subscriber sendCompleted];
        } else {
            NSError *error = [NSError errorWithDomain:YdkNetworkErrorDomain code:YdkNetworkErrorResponseDataInvalid userInfo:@{NSLocalizedDescriptionKey : @"接口响应数据格式错误", YdkNetworkResponseObjectKey : [responseObject isEqual:[NSNull null]] ? @{} : responseObject }];
            [subscriber sendError:error];
        }
        return nil;
    }];
}

- (RACSignal<id> *)handleErrorResponse:(NSInteger)code responseObject:(id)responseObject {
    if (_interceptorFlags.handleError || _interceptorFlags.handleErrorResponseObject) {
        RACSignal<id> *signal;
        if (_interceptorFlags.handleError) {
            signal = [_interceptor handleErrorResponse:code];
        } else {
            signal = [_interceptor handleErrorResponse:code responseObject:responseObject];
        }
        return signal;
    } else {
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            NSMutableDictionary *userInfo = [NSMutableDictionary dictionaryWithDictionary:responseObject];
            userInfo[NSLocalizedDescriptionKey] = @"无法处理响应数据异常，请调用[YdkNetwork registerNetworkInterceptor:] 方法注册拦截器";
            NSError *error = [NSError errorWithDomain:YdkNetworkErrorDomain code:YdkNetworkErrorUnableHandleErrorResponse userInfo:userInfo];
            [subscriber sendError:error];
            return nil;
        }];
    }
}

// MARK: - Utils
- (YdkRequest *)requestWithMethod:(YdkHTTPMethod)method URLString:(NSString *)URLString parameters:(id)parameters {
    if (![URLString hasPrefix:@"http"] && self.config.baseUrl) {
        // 拼接base url
        NSString *baseUrl = [self.config.baseUrl copy];
        if ([baseUrl hasSuffix:@"/"]) {
            // 1. 确保base url不以"/"结束
            baseUrl = [baseUrl substringToIndex:baseUrl.length - 1];
        }
        if (![URLString hasPrefix:@"/"]) {
            // 2. 确保path以"/开始"
            URLString = [NSString stringWithFormat:@"/%@", URLString];
        }
        URLString = [NSString stringWithFormat:@"%@%@", baseUrl, URLString];
    }
    return [[YdkRequest alloc] initWithMethod:method Url:URLString parameters:parameters];
}

- (void)logMessage:(NSString *)message{
    if (self.debugMode) {
        fprintf(stderr, "\n%s\n", message.UTF8String);
    }
}

// MARK: - HTTP Download Service
/**
 HTTP 下载
 
 @param sourceURL 远程文件URL eg: https://cdn.yryz.com/yryz-new/image/3C2E31BF-C7E0-4DB8-A225-0420DF37A642.jpg
 @return 下载信息
 */
+ (RACSignal<YdkDownloadInfo *> *)downloadWithSourceURL:(NSURL *)sourceURL {
    return [self downloadWithSourceURL:sourceURL targetURL:nil];
}

/**
 HTTP 下载
 
 @param sourceURL 远程文件URL eg: https://cdn.yryz.com/yryz-new/image/3C2E31BF-C7E0-4DB8-A225-0420DF37A642.jpg
 @param targetURL 目标目录URL，未传默认放在沙盒Cache文件下 eg: ../cache/
 @return 下载信息
 */
+ (RACSignal<YdkDownloadInfo *> *)downloadWithSourceURL:(NSURL *)sourceURL targetURL:(NSURL *)targetURL {
    YdkNetwork *module = ydk_get_module_instance(YdkNetwork.class);
    return [module.httpService downloadWithSourceURL:sourceURL targetURL:targetURL];
}


// MARK: - OSS Upload Service
/**
 上传图片
 
 @param uploadData 上传图片数据
 @return 上传结果
 */
+ (RACSignal<YdkUploadInfo *> *)uploadWithData:(NSData *)uploadData {
    return [self uploadWithData:uploadData fileType:nil];
}

/**
 上传音视频
 
 @param fileURL 文件地址
 @return 上传结果
 */
+ (RACSignal<YdkUploadInfo *> *)uploadWithFileURL:(NSURL *)fileURL {
    return [self uploadWithFileURL:fileURL fileType:nil];
}

/**
 上传图片
 
 @param uploadData 上传图片数据
 @param fileType 上传数据类型 head（头像），image（图片），audio（音频），video（视频）
 @return 上传结果
 */
+ (RACSignal<YdkUploadInfo *> *)uploadWithData:(NSData *)uploadData fileType:(NSString *)fileType {
    if (!uploadData) {
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            NSError *error = [NSError errorWithDomain:YdkNetworkErrorDomain code:YdkNetworkErrorInvalidUploadData userInfo:@{@"message" : @"无效的uploadData."}];
            [subscriber sendError:error];
            return nil;
        }];
    }
    YdkNetwork *module = ydk_get_module_instance(YdkNetwork.class);
    return [module.uploadService uploadWithData:uploadData fileType:fileType];
}

/**
 上传文件
 
 @param fileURL 文件地址
 @param fileType 上传数据类型 head（头像），image（图片），audio（音频），video（视频）
 @return 上传结果
 */
+ (RACSignal<YdkUploadInfo *> *)uploadWithFileURL:(NSURL *)fileURL fileType:(NSString *)fileType {
    if (!fileURL) {
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            NSError *error = [NSError errorWithDomain:YdkNetworkErrorDomain code:YdkNetworkErrorInvalidFileURL userInfo:@{@"message" : @"无效的fileURL."}];
            [subscriber sendError:error];
            return nil;
        }];
    }
    if (!fileURL.isFileURL) {
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            NSError *error = [NSError errorWithDomain:YdkNetworkErrorDomain code:YdkNetworkErrorInvalidFileURL userInfo:@{@"message" : @"fileURL不是有效的文件URL."}];
            [subscriber sendError:error];
            return nil;
        }];
        
    }
    YdkNetwork *module = ydk_get_module_instance(YdkNetwork.class);
    return [module.uploadService uploadWithFileURL:fileURL fileType:fileType];
}

@end
