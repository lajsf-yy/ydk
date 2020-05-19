
#import "NSDictionary+Ydk.h"
#import "NSObject+Ydk.h"
#import <objc/runtime.h>
@implementation NSDictionary (Ydk)

- (id)getKeyFromValue:(id)value{
    __block id aKey;
    [self enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
        if([value isEqualTo:obj]){
            aKey = key;
            *stop = YES;
        }
        
    }];
    return aKey;
}
-(id) toObject:(Class) klass{
    return [self toObject:klass prefix:@""];
}
-(id) toObject:(Class) klass prefix:(NSString *)prefix{
    id obj = [[klass alloc] init];
    for (NSString *key in [obj propertyKeys]) {
        NSString* dictKey = [prefix stringByAppendingString:key] ;
        id propertyValue = [self valueForKeyPath:dictKey ];
        //该值不为NSNULL，并且也不为nil
        if (![propertyValue isKindOfClass:[NSNull class]] && propertyValue!=nil) {
            [obj setValue:propertyValue forKey:key];
        }
        
    }
    return obj;
}
@end



