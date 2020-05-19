

#import <Foundation/Foundation.h>

@interface NSDictionary (Ydk)

/**
 根据value查找key
 
 @return 转换后的字符串
 */
- (id)getKeyFromValue:(id)value;
-(id) toObject:(Class) klass;
-(id) toObject:(Class) klass prefix:(NSString *)prefix;
@end
