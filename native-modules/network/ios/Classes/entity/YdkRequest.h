//
//  YdkRequest.h
//  ydk
//
//  Created by 悠然一指 on 2018/3/15.
//  Copyright © 2018年 悠然一指. All rights reserved.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSUInteger, YdkHTTPMethod) {
    GET,
    POST,
    DELETE,
    PUT,
    PATCH
};

@interface YdkRequest : NSObject <NSCopying>

@property (nonatomic, assign) YdkHTTPMethod method;
@property (nonatomic, copy) NSString *url;
@property (nonatomic, copy) id parameters;
@property (nonatomic, strong) NSMutableDictionary<NSString *, NSString *> *allHTTPHeaderFields;

- (instancetype)initWithMethod:(YdkHTTPMethod)method Url:(NSString *)url parameters:(id)parameters;

@end
