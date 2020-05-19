

#import <Foundation/Foundation.h>

@interface NSObject (Ydk)

/**
 判断对象是否相等
 
 @return 转换后的字符串
 */
- (BOOL)isEqualTo:(id)other;
- (NSDictionary *)toDictionary;
- (NSArray*)propertyKeys;
@end
