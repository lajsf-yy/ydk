//
//  NSURL+toolkit.h
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import <Foundation/Foundation.h>

@interface NSURL (toolkit)

@property (readonly, copy) NSString *tk_UTI;   // 文件UTI

- (BOOL)tk_isImageFile;

- (BOOL)tk_isVideoFile;

- (BOOL)tk_isAudioFile;

@end
