#import "YdkAppDelegate.h"
#import "YdkModule.h"
#include <objc/runtime.h>

#ifdef NSFoundationVersionNumber_iOS_9_x_Max
#import <UserNotifications/UserNotifications.h>
#endif

NSString *const YdkCancel = @"0";

static NSMutableArray<Class> *ydkModuleClasses;
NSMutableArray<id<YdkModule>> *ydkModules;
static dispatch_queue_t ydkModuleClassesSyncQueue;

void ydk_register_module(Class moduleClass) {
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        ydkModuleClasses = [NSMutableArray new];
        ydkModules = [NSMutableArray new];
        ydkModuleClassesSyncQueue = dispatch_queue_create("ydk.react.ModuleClassesSyncQueue", DISPATCH_QUEUE_CONCURRENT);
    });
    
    dispatch_barrier_async(ydkModuleClassesSyncQueue, ^{
        [ydkModuleClasses addObject:moduleClass];
    });
}

id ydk_get_module_instance(Class moduleClass) {
    for (id<YdkModule> module in ydkModules) {
        if ([module isKindOfClass:moduleClass])
            return module;
    }
    return nil;
}

@implementation YdkAppDelegate

- (BOOL)application:(UIApplication*)application willFinishLaunchingWithOptions:(NSDictionary*)launchOptions {
    return YES;
}

- (BOOL)application:(UIApplication*)application didFinishLaunchingWithOptions:(NSDictionary*)launchOptions {
    NSURL *fileURL = [[NSBundle mainBundle] URLForResource:@"env" withExtension:@"json"];
    NSData *data = [[NSData alloc] initWithContentsOfURL:fileURL];
    _config = [NSJSONSerialization JSONObjectWithData:data options:kNilOptions error:nil];
    for(Class cls in ydkModuleClasses){
        id<YdkModule> module = nil;
        Method initMethod = class_getInstanceMethod(cls, @selector(initWithConfig:));
        if (initMethod != nil) {
            module = [[cls alloc] initWithConfig:_config];
        } else {
            module = [[cls alloc] init];
        }
        [ydkModules addObject:module];
    }
    NSMutableArray *arguments = [NSMutableArray array];
    [arguments addObject:application];
    if (launchOptions) {
        [arguments addObject:launchOptions];
    }
    [self performBatchModuleSelector:@selector(application:didFinishLaunchingWithOptions:) arguments:arguments];
    return YES;
}

#pragma GCC diagnostic push
#pragma GCC diagnostic ignored "-Wdeprecated-declarations"
- (void)application:(UIApplication*)application didRegisterUserNotificationSettings:(UIUserNotificationSettings*)notificationSettings {
    [self performBatchModuleSelector:@selector(application:didRegisterUserNotificationSettings:) arguments:@[application, notificationSettings]];
}
#pragma GCC diagnostic pop

- (void)application:(UIApplication*)application didRegisterForRemoteNotificationsWithDeviceToken:(NSData*)deviceToken {
    [self performBatchModuleSelector:@selector(application:didRegisterForRemoteNotificationsWithDeviceToken:) arguments:@[application, deviceToken]];
}

- (void)application:(UIApplication*)application didReceiveRemoteNotification:(NSDictionary*)userInfo fetchCompletionHandler:(void (^)(UIBackgroundFetchResult result))completionHandler {
    [self performBatchModuleSelector:@selector(application:didReceiveRemoteNotification:fetchCompletionHandler:) arguments:@[application, userInfo, completionHandler]];
}

- (void)application:(UIApplication*)application didReceiveLocalNotification:(UILocalNotification*)notification {
    [self performBatchModuleSelector:@selector(application:didReceiveLocalNotification:) arguments:@[application, notification]];
}

- (void)userNotificationCenter:(UNUserNotificationCenter*)center willPresentNotification:(UNNotification*)notification withCompletionHandler:(void (^)(UNNotificationPresentationOptions options))completionHandler API_AVAILABLE(ios(10)) {
    if (@available(iOS 10.0, *)) {
        [self performBatchModuleSelector:@selector(userNotificationCenter:willPresentNotification:withCompletionHandler:) arguments:@[center, notification, completionHandler]];
    }
}

- (void)userNotificationCenter:(UNUserNotificationCenter *)center didReceiveNotificationResponse:(UNNotificationResponse *)response withCompletionHandler:(void(^)(void))completionHandler API_AVAILABLE(ios(10))  {
    if (@available(iOS 10.0, *)) {
        [self performBatchModuleSelector:@selector(userNotificationCenter:didReceiveNotificationResponse:withCompletionHandler:) arguments:@[center, response, completionHandler]];
    }
}

- (BOOL)application:(UIApplication*)application openURL:(NSURL*)url options:(NSDictionary<UIApplicationOpenURLOptionsKey, id>*)options {
    [self performBatchModuleSelector:@selector(application:openURL:options:) arguments:@[application, url, options]];
    return YES;
}

- (BOOL)application:(UIApplication *)application continueUserActivity:(NSUserActivity *)userActivity restorationHandler:(void(^)(NSArray<id<UIUserActivityRestoring>> * __nullable restorableObjects))restorationHandler {
    [self performBatchModuleSelector:@selector(application:continueUserActivity:restorationHandler:) arguments:@[application, userActivity, restorationHandler]];
    return YES;
}

// MARK: - Utils
- (void)performBatchModuleSelector:(SEL)selector arguments:(NSArray *)arguments {
    for (id<YdkModule> module in ydkModules) {
        if ([module respondsToSelector:selector]) {
            [self invokeWithTarget:module selector:selector arguments:arguments];
        }
    }
}

- (void)invokeWithTarget:(id)target selector:(SEL)selector arguments:(NSArray *)arguments {
    NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:[target methodSignatureForSelector:selector]];
    invocation.selector = selector;
    invocation.target = target;
    
    for (NSUInteger i = 0; i < arguments.count; i++) {
        id arg = arguments[i];
        NSInteger argIndex = (NSInteger)(i + 2);
        [invocation setArgument:&arg atIndex:argIndex];
    }
    [invocation invoke];
//    __unsafe_unretained id returnVal;
//    [invocation getReturnValue:&returnVal];
//    return returnVal;
}

@end
