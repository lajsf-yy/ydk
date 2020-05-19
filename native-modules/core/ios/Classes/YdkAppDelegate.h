#import <UIKit/UIKit.h>
#import <Foundation/Foundation.h>
#import "YdkModule.h"
#import "YdkConstants.h"
void ydk_register_module(Class);
id ydk_get_module_instance(Class);
@interface YdkAppDelegate : UIResponder <UIApplicationDelegate>
@property (nonatomic, strong) UIWindow *window;
@property (readonly, nonatomic, copy) NSDictionary *config;
@end
