//
//  YdkAudioPlayerManager.h
//  ydk-audio
//
//  Created by yryz on 2019/7/10.
//

#import <Foundation/Foundation.h>
#import "YdkAudioPlayerProtocol.h"

@interface YdkAudioPlayerManager : NSObject

@property (readonly, nonatomic, strong) id<YdkAudioPlayerControl> player;

+ (instancetype)sharedInstance;

/**
 调用播放音频服务

 @param url 音频uri
 @param tagId 单个播放唯一标识，可以理解为页面上的单个播放控件的唯一标识。当tagId值不同时则会主动触发上次播放控件的停止回调，如果相同则无需触发。可以使用当前时间戳。
 @param delegate 状态回调
 */
+ (void)playWithURL:(NSURL *)url tagId:(NSNumber *)tagId delegate:(id<YdkAudioPlayerDelegate>)delegate;

@end
