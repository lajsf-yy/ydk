//
//  YdkOssUploadService.h
//  ydk
//
//  Created by yryz on 2019/6/19.
//

#import <Foundation/Foundation.h>
#import "YdkNetworkServiceProtocol.h"

@interface YdkOssUploadService : NSObject <YdkOSSUploadServiceProtocol>

- (instancetype)initWithAccessKeyId:(NSString *)accessKeyId secretAccessKey:(NSString *)secretAccessKey bucketName:(NSString *)bucketName directoryName:(NSString *)name ossHost:(NSString *)ossHost;

@end
