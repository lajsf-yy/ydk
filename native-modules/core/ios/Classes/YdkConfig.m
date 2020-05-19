#import "YdkConfig.h"
#import "YdkAppDelegate.h"
@implementation YdkConfig
static NSDictionary *envConfig = nil;
+ (void)load {
  ydk_register_module(self);
}
+ (instancetype)shared {
    return  ydk_get_module_instance([self class]);
}
- (instancetype)initWithConfig:(NSDictionary *)config {
    envConfig=config;
   
    if (self = [super init]) {
       
    }
    _webBaseUrl = [config valueForKeyPath:@"webBaseUrl"];
    _httpBaseUrl = [config valueForKeyPath:@"httpBaseUrl"];
    return self;
}
- (nullable id)valueForKeyPath:(NSString *)keyPath{
    return [envConfig valueForKeyPath:keyPath];
}

@end
