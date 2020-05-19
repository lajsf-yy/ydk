
#import "NSObject+Ydk.h"
#import <objc/runtime.h>
@implementation NSObject (Ydk)
- (BOOL)isEqualTo:(id)other{
    if (other == self) return YES;
    if (!other || ![other isKindOfClass:[self class]]) return NO;
    if([other isKindOfClass:[NSNumber class]] && [other isEqualToNumber:(id)self]){
        return YES;
    }
    if([other isKindOfClass:[NSString class]] && [other isEqualToString:(id)self]){
        return YES;
    }
    return  [self isEqual:other];
}

- (NSDictionary *)toDictionary {
    NSMutableDictionary *dictionary = [NSMutableDictionary new];
    for (NSString *key in [self propertyKeys]){
        id value = [self valueForKey:key];
        
        if (value == nil) {
            // nothing todo
        }
        else if ([value isKindOfClass:[NSNumber class]]
                 || [value isKindOfClass:[NSString class]]
                 || [value isKindOfClass:[NSDictionary class]]) {
            // TODO: extend to other types
            [dictionary setObject:value forKey:key];
        }
        else if ([value isKindOfClass:[NSObject class]]) {
            [dictionary setObject:[value toDictionary] forKey:key];
        }
        else {
            NSLog(@"Invalid type for %@ (%@)", NSStringFromClass([self class]), key);
        }
    }
    return dictionary;
}
-(NSArray*)propertyKeys
{
    unsigned int outCount, i;
    objc_property_t *properties = class_copyPropertyList([self class], &outCount);
    NSMutableArray *keys = [[NSMutableArray alloc] initWithCapacity:outCount];
    for (i = 0; i < outCount; i++) {
        NSString *propertyName =[NSString stringWithUTF8String:property_getName(properties[i])];
        [keys addObject:propertyName];
    }
    free(properties);
    return keys;
    
}
@end


