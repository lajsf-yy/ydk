//
//  YdkDownloadInfo.h
//  ydk
//
//  Created by 悠然一指 on 2018/4/18.
//  Copyright © 2018年 悠然一指. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface YdkDownloadInfo : NSObject

@property (nonatomic, assign) int64_t totalBytesReceive;                // 已下载字节数
@property (nonatomic, assign) int64_t totalBytesExpectedToReceive;      // 总下载字节数

@property (nonatomic, assign, getter=isCompleted) BOOL completed;
@property (readonly, nonatomic, assign) float progress; // 0.0 ~ 1.0

@property (nonatomic, copy) NSString *url;

@end
