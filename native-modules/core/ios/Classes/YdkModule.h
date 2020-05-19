#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@protocol YdkModule <NSObject>

@optional
-(_Nullable instancetype)initWithConfig:(NSDictionary * _Nonnull)config;
/**
 * Called if this plugin has been registered for `UIApplicationDelegate` callbacks.
 *
 * @return `NO` if this plugin vetoes application launch.
 */
- (BOOL)application:(UIApplication * _Nonnull)application
    didFinishLaunchingWithOptions:(NSDictionary * _Nullable)launchOptions;
		/**
 * Called if this plugin has been registered for `UIApplicationDelegate` callbacks.
 */

- (void)application:(UIApplication * _Nonnull)application
    didRegisterUserNotificationSettings:(UIUserNotificationSettings * _Nullable)notificationSettings;


/**
 * Called if this plugin has been registered for `UIApplicationDelegate` callbacks.
 */
- (void)application:(UIApplication * _Nonnull)application
    didRegisterForRemoteNotificationsWithDeviceToken:(NSData * _Nullable)deviceToken;
		/**
 * Called if this plugin has been registered for `UIApplicationDelegate` callbacks.
 *
 * @return `YES` if this plugin handles the request.
 */
- (BOOL)application:(UIApplication * _Nonnull)application
    didReceiveRemoteNotification:(NSDictionary * _Nullable)userInfo
          fetchCompletionHandler:(void (^)(UIBackgroundFetchResult result))completionHandler;

/**
 * Calls all plugins registered for `UIApplicationDelegate` callbacks.
 */
- (void)application:(UIApplication * _Nonnull)application
    didReceiveLocalNotification:(UILocalNotification * _Nullable)notification;



/**
 * Called if this plugin has been registered for `UIApplicationDelegate` callbacks.
 *
 * @return `YES` if this plugin handles the request.
 */
- (BOOL)application:(UIApplication * _Nonnull)application
            openURL:(NSURL * _Nullable)url
            options:(NSDictionary<UIApplicationOpenURLOptionsKey, id> * _Nullable)options;
/**
 * Called if this plugin has been registered for `UIApplicationDelegate` callbacks.
 *
 * @return `YES` if this plugin handles the request.
 */
- (BOOL)application:(UIApplication * _Nonnull)application handleOpenURL:(NSURL * _Nullable)url;

/**
 * Called if this plugin has been registered for `UIApplicationDelegate` callbacks.
 *
 * @return `YES` if this plugin handles the request.
 */
- (BOOL)application:(UIApplication * _Nonnull)application
              openURL:(NSURL * _Nullable)url
    sourceApplication:(NSString * _Nullable)sourceApplication
           annotation:(id _Nullable)annotation;

/**
 * Called if this plugin has been registered for `UIApplicationDelegate` callbacks.
 *
 * @return `YES` if this plugin handles the request.
 */
- (BOOL)application:(UIApplication * _Nonnull)application
performActionForShortcutItem:(UIApplicationShortcutItem * _Nullable)shortcutItem
  completionHandler:(void (^)(BOOL succeeded))completionHandler
    API_AVAILABLE(ios(9.0));

/**
 * Called if this plugin has been registered for `UIApplicationDelegate` callbacks.
 *
 * @return `YES` if this plugin handles the request.
 */
- (BOOL)application:(UIApplication * _Nonnull)application
    handleEventsForBackgroundURLSession:(NSString * _Nullable)identifier
                      completionHandler:(void (^)(void))completionHandler;

/**
 * Called if this plugin has been registered for `UIApplicationDelegate` callbacks.
 *
 * @return `YES` if this plugin handles the request.
 */
- (BOOL)application:(UIApplication * _Nonnull)application
performFetchWithCompletionHandler:(void (^)(UIBackgroundFetchResult result))completionHandler;

/**
 * Called if this plugin has been registered for `UIApplicationDelegate` callbacks.
 *
 * @return `YES` if this plugin handles the request.
 */
- (BOOL)application:(UIApplication * _Nonnull)application continueUserActivity:(NSUserActivity * _Nullable)userActivity restorationHandler:(void (^)(NSArray*))restorationHandler;

@end

NS_ASSUME_NONNULL_END
