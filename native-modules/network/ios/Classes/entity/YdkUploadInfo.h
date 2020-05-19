//
//  YdkUploadInfo.h
//  ydk
//
//  Created by 悠然一指 on 2018/3/16.
//  Copyright © 2018年 悠然一指. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface YdkUploadInfo : NSObject

@property (nonatomic, assign) int64_t totalBytesSent;               // 已上传字节数
@property (nonatomic, assign) int64_t totalBytesExpectedToSend;     // 总上传字节数

@property (nonatomic, assign, getter=isCompleted) BOOL completed;
@property (readonly, nonatomic, assign) float progress; // 0.0 ~ 1.0

@property (nonatomic, copy) NSString *url;

@property (nonatomic, copy) NSDictionary *ext;  // 扩展字段

@end

