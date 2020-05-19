//
//  YdkHTTPSerivce.m
//  ydk
//
//  Created by yryz on 2019/6/19.
//

#import "YdkHTTPSerivce.h"
#import "YdkDownloadInfo.h"
#import "YdkJSONResponseSerializer.h"
#import <AFNetworking/AFNetworking.h>

@implementation YdkHTTPSerivce

- (RACSignal<id> *)request:(YdkRequest *)request {
    RACSubject *subject = [RACSubject subject];
    AFHTTPSessionManager *session = [self sessionManagerWithRequest:request];
    void (^successBlock)(NSURLSessionDataTask *task, id responseObject) = ^(NSURLSessionDataTask *task, id responseObject) {
        [subject sendNext:responseObject];
        [subject sendCompleted];
    };
    void (^failureBlock)(NSURLSessionDataTask *task, NSError * error) = ^(NSURLSessionDataTask *task, NSError * e) {
        NSMutableDictionary *userInfo = [NSMutableDictionary dictionaryWithDictionary:e.userInfo];
        if (task) [userInfo setObject:task forKey:YdkNSURLSessionDataTaskKey];
        NSError *error = [NSError errorWithDomain:e.domain code:e.code userInfo:userInfo];
        [subject sendError:error];
    };
    switch (request.method) {
        case GET:
            [session GET:request.url parameters:request.parameters progress:nil success:successBlock failure:failureBlock];
            break;
        case POST:
            [session POST:request.url parameters:request.parameters progress:nil success:successBlock failure:failureBlock];
            break;
        case DELETE:
            [session DELETE:request.url parameters:request.parameters success:successBlock failure:failureBlock];
            break;
        case PUT:
            [session PUT:request.url parameters:request.parameters success:successBlock failure:failureBlock];
            break;
        case PATCH:
            [session PATCH:request.url parameters:request.parameters success:successBlock failure:failureBlock];
            break;
    }
    return subject;
}

- (AFHTTPSessionManager *)sessionManagerWithRequest:(YdkRequest *)request {
    AFHTTPSessionManager *session = [AFHTTPSessionManager manager];
    if (request.method == POST || request.method == PUT || request.method == DELETE) {
        AFJSONRequestSerializer *requestSerializer = [AFJSONRequestSerializer serializer];
        requestSerializer.HTTPMethodsEncodingParametersInURI = [NSSet setWithObjects:@"GET", @"HEAD", nil];
        session.requestSerializer = requestSerializer;
    }
    session.responseSerializer = [YdkJSONResponseSerializer serializer];
    session.requestSerializer.timeoutInterval = 15;
    // text/plan
    NSMutableSet *contentTypes = [session.responseSerializer.acceptableContentTypes mutableCopy];
    [contentTypes addObject:@"text/plain"];
    session.responseSerializer.acceptableContentTypes = contentTypes;
    // acceptableStatusCodes
    session.responseSerializer.acceptableStatusCodes = [NSIndexSet indexSetWithIndexesInRange:NSMakeRange(200, 202)];   // for 401
    
    __block BOOL stopEqual = NO;
    [request.allHTTPHeaderFields enumerateKeysAndObjectsUsingBlock:^(NSString *key, id obj, BOOL *stop) {
        if (!stopEqual && [key.lowercaseString isEqualToString:@"content-type"]) {
            stopEqual = YES;
            if ([obj isKindOfClass:[NSString class]] && [((NSString *)obj).lowercaseString rangeOfString:@"json"].location != NSNotFound) {
                session.requestSerializer = [AFJSONRequestSerializer serializer];
            } else {
                session.requestSerializer = [AFHTTPRequestSerializer serializer];
            }
        }
        [session.requestSerializer setValue:obj forHTTPHeaderField:key];
    }];
    return session;
}

- (RACSignal<YdkDownloadInfo *> *)downloadWithSourceURL:(NSURL *)sourceURL targetURL:(NSURL *)targetURL {
    if (!sourceURL) {
        return [RACSignal createSignal:^RACDisposable * (id<RACSubscriber> subscriber) {
            [subscriber sendError:[NSError errorWithDomain:YdkNetworkErrorDomain code:YdkNetworkErrorInvalidSourceURL userInfo:@{NSLocalizedDescriptionKey : @"无效的文件sourceURL"}]];
            return nil;
        }];
    }
    if (!targetURL) {
        targetURL = [self createCacheFileURLWithComponent:NSStringFromClass(self.class)];
    }
    
    NSURLSessionConfiguration *configuration = [NSURLSessionConfiguration defaultSessionConfiguration];
    AFURLSessionManager *manager = [[AFURLSessionManager alloc] initWithSessionConfiguration:configuration];
    NSURLRequest *request = [NSURLRequest requestWithURL:sourceURL];
    return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
        YdkDownloadInfo *info = [[YdkDownloadInfo alloc] init];
        NSURLSessionDownloadTask *downloadTask = [manager downloadTaskWithRequest:request progress:^(NSProgress *downloadProgress) {
            info.totalBytesReceive = downloadProgress.completedUnitCount;
            info.totalBytesExpectedToReceive = downloadProgress.totalUnitCount;
            [subscriber sendNext:info];
        } destination:^NSURL *(NSURL *targetPath, NSURLResponse *response) {
            NSURL *destinationURL = [targetURL URLByAppendingPathComponent:[response suggestedFilename]];
            return destinationURL;
        } completionHandler:^(NSURLResponse *response, NSURL *filePath, NSError *error) {
            if (error) {
                [subscriber sendError:error];
            } else {
                info.completed = YES;
                info.url = filePath.path;
                [subscriber sendNext:info];
            }
            [subscriber sendCompleted];
        }];
        [downloadTask resume];
        return nil;
    }];
}

// MARK: - Utils
- (NSURL *)createCacheFileURLWithComponent:(NSString *)component {
    NSString *filePath = [self cachesDirectoryForComponent:component];
    return [self createFileURLWithFilePath:filePath];
}

- (NSString *)cachesDirectoryForComponent:(NSString *)component {
    NSString *subdir = [self escapedResourceName:component];
    return [[NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, YES).firstObject
             stringByAppendingPathComponent:@"ydk"]
            stringByAppendingPathComponent:subdir];
}

- (NSString *)escapedResourceName:(NSString *)name {
    NSString *charactersToEscape = @"!*'();:@&=+$,/?%#[]";
    NSCharacterSet *allowedCharacters = [[NSCharacterSet characterSetWithCharactersInString:charactersToEscape] invertedSet];
    return [name stringByAddingPercentEncodingWithAllowedCharacters:allowedCharacters];
}

- (NSURL *)createFileURLWithFilePath:(NSString *)filePath {
    NSString *directory = filePath;
    if (filePath.pathExtension.length > 0) {
        directory = [filePath stringByDeletingLastPathComponent];
    }
    if (![[NSFileManager defaultManager] fileExistsAtPath:directory]) {
        [[NSFileManager defaultManager] createDirectoryAtPath:directory withIntermediateDirectories:YES attributes:nil error:nil];
    }
    
    BOOL isDirectory;
    BOOL isExists = [[NSFileManager defaultManager] fileExistsAtPath:filePath isDirectory:&isDirectory];
    if (isExists && !isDirectory) {
        [[NSFileManager defaultManager] removeItemAtPath:filePath error:nil];
    }
    return [NSURL fileURLWithPath:filePath];
}

@end
