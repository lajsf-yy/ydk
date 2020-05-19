//
//  NSString+toolkit.m
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import "NSString+toolkit.h"

@implementation NSString (toolkit)

- (NSString *)tk_stringByReplacingJSONSpecialCharacters {
    NSRange range = [self rangeOfString:@"\'"];
    NSString *jsonString = [self copy];
    if (range.location != NSNotFound) {
        jsonString = [jsonString stringByReplacingOccurrencesOfString:@"\'" withString:@"\\'"];
    }
    return jsonString;
}

+ (NSString *)convertToJsonData:(NSDictionary *)dict {
    
    NSError *error;
    NSString *jsonString;
    NSData *jsonData = [NSJSONSerialization dataWithJSONObject:dict options:NSJSONWritingPrettyPrinted error:&error];
    
    if (!jsonData) {
        NSLog(@"%@",error);
    }else{
        jsonString = [[NSString alloc]initWithData:jsonData encoding:NSUTF8StringEncoding];
    }
    
    NSMutableString *mutStr = [NSMutableString stringWithString:jsonString];
    NSRange range = {0,jsonString.length};
    
    //去掉字符串中的空格
    [mutStr replaceOccurrencesOfString:@" " withString:@"" options:NSLiteralSearch range:range];
    NSRange range2 = {0,mutStr.length};
    
    //去掉字符串中的换行符
    [mutStr replaceOccurrencesOfString:@"\n" withString:@"" options:NSLiteralSearch range:range2];
    
    return mutStr;
}

+ (NSString *)tk_randomUUID {
    CFUUIDRef uuid = CFUUIDCreate(NULL);
    CFStringRef strRef = CFUUIDCreateString(NULL, uuid);
    NSString *uuidString = (__bridge NSString *)strRef;
    CFRelease(uuid);
    CFRelease(strRef);
    return uuidString;
}

@end
