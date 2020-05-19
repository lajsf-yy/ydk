//
//  YdkDownloadInfo.m
//  ydk
//
//  Created by 悠然一指 on 2018/4/18.
//  Copyright © 2018年 悠然一指. All rights reserved.
//

#import "YdkDownloadInfo.h"

@implementation YdkDownloadInfo

- (float)progress {
    return (float)_totalBytesReceive / _totalBytesExpectedToReceive;
}

- (NSString *)description {
    return [NSString stringWithFormat:@"Progress: %.2f, URL: %@", self.progress, _url];
}

@end
