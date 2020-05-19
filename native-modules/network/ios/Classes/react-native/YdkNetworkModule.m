//
//  YdkNetworkModule.m
//  ydk-network
//
//  Created by yryz on 2019/6/19.
//

#import "YdkNetworkModule.h"
#import "YdkNetwork.h"

#import <ydk-toolkit/YdkToolkit.h>

static NSString *kUploadProcess = @"uploadProcess";
static NSString *kDownloadProcess = @"downloadProcess";

@implementation YdkNetworkModule
{
    BOOL _hasListeners;
}

- (void)startObserving {
    _hasListeners = YES;
}

- (void)stopObserving {
    _hasListeners = NO;
}

- (void)sendEventWithName:(NSString *)name body:(id)body {
    if (_hasListeners) {
        [super sendEventWithName:name body:body];
    }
}

RCT_EXPORT_MODULE(YdkNetworkModule)

- (NSDictionary *)constantsToExport {
    return @{ kUploadProcess: kUploadProcess,
              kDownloadProcess: kDownloadProcess
              };
}

- (NSArray<NSString *> *)supportedEvents {
    return @[kUploadProcess, kDownloadProcess];
}

+ (BOOL)requiresMainQueueSetup {
    return true;
}

// 上传
RCT_EXPORT_METHOD(upload:(NSString *)filePath resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    [self upload:filePath fileType:nil resolver:resolver rejecter:rejecter];
}

RCT_EXPORT_METHOD(upload:(NSString *)filePath fileType:(NSString *)fileType resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    NSURL *fileURL = [NSURL fileURLWithPath:filePath];
    @weakify(self);
    void (^subscribeNext) (YdkUploadInfo *info) = ^(YdkUploadInfo *info) {
        @strongify(self);
        if (info.isCompleted) {
            NSMutableDictionary *result = [@{@"localFile" : filePath, @"url" : info.url} mutableCopy];
            NSString *sizeString = [info.ext objectForKey:@"size"];
            if (sizeString) {
                CGSize size = CGSizeFromString(sizeString);
                [result addEntriesFromDictionary:@{@"width" : @(size.width), @"height" : @(size.height)}];
            }
            resolver(result);
        } else {
            [self sendEventWithName:kUploadProcess body:@{ @"filePath" : filePath, @"total" : @(info.totalBytesExpectedToSend), @"uploadBytes" : @(info.totalBytesSent) }];
        }
    };
    void (^error) (NSError *error) = ^(NSError *error) {
        rejecter(@(error.code).stringValue, [error.userInfo objectForKey:NSLocalizedDescriptionKey], error);
    };
    
    if (fileURL.tk_isImageFile) {
        NSData *data = [[UIImage imageWithContentsOfFile:filePath] tk_compressImageWithMaxLength:KB512];
        [[YdkNetwork uploadWithData:data fileType:fileType] subscribeNext:subscribeNext error:error];
    } else {
        [[YdkNetwork uploadWithFileURL:fileURL fileType:fileType] subscribeNext:subscribeNext error:error];
    }
}

// 下载
RCT_EXPORT_METHOD(download:(NSString *)url resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    NSURL *downloadUrl = [NSURL URLWithString:url];
    [[YdkNetwork downloadWithSourceURL:downloadUrl] subscribeNext:^(YdkDownloadInfo *info) {
        if (info.isCompleted) {
            NSDictionary *result = @{@"url" : url, @"fileName" : info.url.lastPathComponent, @"filePath" : info.url};
            resolver(result);
        } else {
            [self sendEventWithName:kDownloadProcess body:@{ @"url" : url, @"total" : @(info.totalBytesExpectedToReceive), @"downloadBytes" : @(info.totalBytesReceive) }];
        }
    } error:^(NSError *error) {
        rejecter(@(error.code).stringValue, [error.userInfo objectForKey:NSLocalizedDescriptionKey], error);
    }];
}

RCT_EXPORT_METHOD(get:(NSString *)url parameters:(NSDictionary *)parameters resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    [self _request:GET url:url parameters:parameters resolver:resolver rejecter:rejecter];
}

RCT_EXPORT_METHOD(post:(NSString *)url parameters:(id)parameters resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    [self _request:POST url:url parameters:parameters resolver:resolver rejecter:rejecter];
}

RCT_EXPORT_METHOD(delete:(NSString *)url parameters:(id)parameters resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    [self _request:DELETE url:url parameters:parameters resolver:resolver rejecter:rejecter];
}

RCT_EXPORT_METHOD(put:(NSString *)url parameters:(id)parameters resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    [self _request:PUT url:url parameters:parameters resolver:resolver rejecter:rejecter];
}


- (void)_request:(YdkHTTPMethod)method url:(NSString *)url parameters:(id)parameters resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter {
    YdkNetwork *module = ydk_get_module_instance(YdkNetwork.class);
    YdkRequest *request = [module requestWithMethod:method URLString:url parameters:parameters];
    [[[module request:method URLString:url parameters:parameters] flattenMap:^RACSignal *(id value) {
        return [module interceptorsResponse:value request:request];
    }] subscribeNext:^(id x) {
        resolver(x);
    } error:^(NSError *error) {
        rejecter(@(error.code).stringValue, error.localizedDescription, error);
    }];
}

@end
