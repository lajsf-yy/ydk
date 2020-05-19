#import <UIKit/UIKit.h>
#import "YdkEventEmitter.h"
#import "YdkLayoutProtocol.h"

typedef void (^YdkReactViewReadyCompletionBlock)(void);

@interface UIViewController (LayoutProtocol) <YdkLayoutProtocol>

@property (nonatomic, retain) YdkLayoutInfo* layoutInfo;
@property (nonatomic, strong) YdkEventEmitter* eventEmitter;
@property (nonatomic) id<YdkRootViewCreator> creator;

- (void)renderTreeAndWait:(BOOL)wait perform:(YdkReactViewReadyCompletionBlock)readyBlock;

@end
