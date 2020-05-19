
#import "UIViewController+LayoutProtocol.h"
#import "YdkNavigationOptions.h"
#import <objc/runtime.h>

@implementation UIViewController (LayoutProtocol)

- (instancetype)initWithLayoutInfo:(YdkLayoutInfo *)layoutInfo
                           creator:(id<YdkRootViewCreator>)creator
                      eventEmitter:(YdkEventEmitter *)eventEmitter; {
	self = [self init];
	
	self.layoutInfo = layoutInfo;
	self.creator = creator;
	self.eventEmitter = eventEmitter;
	return self;
}

- (void)renderTreeAndWait:(BOOL)wait perform:(YdkReactViewReadyCompletionBlock)readyBlock {
  dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_HIGH, 0), ^{
    dispatch_async(dispatch_get_main_queue(), ^{
      if (readyBlock) readyBlock();
    });
  });
}

#pragma mark getters and setters to associated object

- (YdkLayoutInfo *)layoutInfo {
	return objc_getAssociatedObject(self, @selector(layoutInfo));
}

- (void)setLayoutInfo:(YdkLayoutInfo *)layoutInfo {
	objc_setAssociatedObject(self, @selector(layoutInfo), layoutInfo, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (YdkEventEmitter *)eventEmitter {
	return objc_getAssociatedObject(self, @selector(eventEmitter));
}

- (void)setEventEmitter:(YdkEventEmitter *)eventEmitter {
	objc_setAssociatedObject(self, @selector(eventEmitter), eventEmitter, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (id<YdkRootViewCreator>)creator {
	return objc_getAssociatedObject(self, @selector(creator));
}

- (void)setCreator:(id<YdkRootViewCreator>)creator {
	objc_setAssociatedObject(self, @selector(creator), creator, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

- (void)mergeOptions:(YdkNavigationOptions *)options {
    BOOL popGesture = YES;
    if (options && !options.popGesture) {
        popGesture = NO;
    }
    self.navigationController.interactivePopGestureRecognizer.enabled = popGesture;
}

- (YdkNavigationOptions *)options {
    return objc_getAssociatedObject(self, @selector(options));
}

- (void)setOptions:(YdkNavigationOptions *)options {
    objc_setAssociatedObject(self, @selector(options), options, OBJC_ASSOCIATION_RETAIN_NONATOMIC);
}

@end
