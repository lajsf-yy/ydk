#import <Foundation/Foundation.h>
#import "YdkModule.h"
@interface YdkConfig : NSObject<YdkModule>
+ (instancetype _Nonnull )shared;
- (nullable id)valueForKeyPath:(NSString *_Nonnull)keyPath;
@property (nonatomic, copy) NSString * _Nonnull httpBaseUrl;
@property (nonatomic, copy) NSString * _Nonnull webBaseUrl;
@end
