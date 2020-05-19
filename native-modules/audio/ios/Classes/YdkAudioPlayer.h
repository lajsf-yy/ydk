//
//  YdkAudioPlayer.h
//  ydk-audio
//
//  Created by yryz on 2019/7/10.
//

#import <Foundation/Foundation.h>
#import "YdkAudioPlayerProtocol.h"

@interface YdkAudioPlayer : NSObject <YdkAudioPlayerControl>

@property (nonatomic, weak) id<YdkAudioPlayerDelegate> delegate;

- (void)prepareWithURL:(NSURL *)url autoPlay:(BOOL)autoPlay;

@end
