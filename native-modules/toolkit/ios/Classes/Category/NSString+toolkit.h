//
//  NSString+toolkit.h
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import <Foundation/Foundation.h>


@interface NSString (toolkit)

/**
 替换JSON转换中的特殊字符[\' -> \\']
 
 @return 转换后的字符串
 */
- (NSString *)tk_stringByReplacingJSONSpecialCharacters;



/**
 字典转字符串

 @param dict NSDictionary
 @return NSString
 */
+ (NSString *)convertToJsonData:(NSDictionary *)dict;


/**
 生成随机UUID
 
 @return UUID字符串
 */
+ (NSString *)tk_randomUUID;

@end

