#import "YdkBugly.h"
#import <Bugly/Bugly.h>

@interface YdkBuglyConfig:NSObject
@property (nonatomic, retain)  NSString* appId;
@property (nonatomic, retain)  NSString* appKey;

@end

@implementation YdkBuglyConfig
@end

@implementation YdkBugly{
  YdkBuglyConfig* buglyConfig;
}

+(void)load{
  ydk_register_module(self);
}

- (instancetype)initWithConfig:(NSDictionary *)config{
  self = [super init];
  if (self) {
    buglyConfig = [config toObject:[YdkBuglyConfig class] prefix:@"bugly."];
  }
  return self;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
	[Bugly startWithAppId:buglyConfig.appId];

  return YES;
}

@end
