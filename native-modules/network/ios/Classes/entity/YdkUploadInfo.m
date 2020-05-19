//
//  YdkUploadInfo.m
//  ydk
//
//  Created by 悠然一指 on 2018/3/16.
//  Copyright © 2018年 悠然一指. All rights reserved.
//

#import "YdkUploadInfo.h"

@implementation YdkUploadInfo

- (float)progress {
    return (float)_totalBytesSent / _totalBytesExpectedToSend;
}

@end
