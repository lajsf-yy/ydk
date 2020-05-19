//
//  YdkRequest.m
//  ydk
//
//  Created by 悠然一指 on 2018/3/15.
//  Copyright © 2018年 悠然一指. All rights reserved.
//

#import "YdkRequest.h"

@implementation YdkRequest

- (instancetype)initWithMethod:(YdkHTTPMethod)method Url:(NSString *)url parameters:(id)parameters {
    if (self = [super init]) {
        _method = method;
        _url = [url copy];
        _parameters = [parameters copy];
        _allHTTPHeaderFields = [NSMutableDictionary dictionary];
    }
    return self;
}

// MARK: - NSCopying
- (id)copyWithZone:(NSZone *)zone {
    YdkRequest *request = [[YdkRequest alloc] initWithMethod:_method Url:_url parameters:_parameters];
    request.allHTTPHeaderFields = [_allHTTPHeaderFields mutableCopy];
    return request;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"\nurl: %@\nmethod: %@\nheader: %@\nparams: %@", _url, [self httpMethod:_method], _allHTTPHeaderFields, _parameters];
}

- (NSString *)httpMethod:(YdkHTTPMethod)method {
    switch (method) {
        case GET:
            return @"GET";
            break;
        case POST:
            return @"POST";
            break;
        case DELETE:
            return @"DELETE";
            break;
        case PUT:
            return @"PUT";
            break;
        case PATCH:
            return @"PATCH";
            break;
    }
}

@end
