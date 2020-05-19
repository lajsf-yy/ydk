//
//  YVideoView.h
//  ydk
//
//  Created by 悠然一指 on 2018/6/4.
//  Copyright © 2018年 悠然一指. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "YdkVideoPlayerProtocol.h"

@interface YdkVideoPlayerView : UIView <YdkVideoPlayerControl>

@property (nonatomic, weak) id<YdkVideoPlayerDelegate> delegate;

// { uri: http:// or file://, videoGravity:"aspect/aspectFill/resize" }
@property (nonatomic, copy) NSDictionary *source;

@end
