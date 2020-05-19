//
//  YdkToolkitMacros.h
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#ifndef YdkToolkitMacros_h
#define YdkToolkitMacros_h

#define TK_DESIGN_PT_W 375.0f
#define TK_DESIGN_PT_H 667.0f

#define TK_TransformPT_W(pt) ((pt) * ([UIScreen mainScreen].bounds.size.width / TK_DESIGN_PT_W))
#define TK_TransformPT_H(pt) ((pt) * ([UIScreen mainScreen].bounds.size.height / TK_DESIGN_PT_H))

#define TK_SCREEN_WIDTH [[UIScreen mainScreen] bounds].size.width
#define TK_SCREEN_HEIGHT [[UIScreen mainScreen] bounds].size.height

#define TK_STATUS_BAR_HEIGHT ((![[UIApplication sharedApplication] isStatusBarHidden]) ? [[UIApplication sharedApplication] statusBarFrame].size.height : (TK_IS_iPhoneX_Series ? 44.f : 20.f))

#define TK_IS_iPhoneX_XS (TK_SCREEN_HEIGHT == 812.f)
#define TK_IS_iPhoneXR_XSMax (TK_SCREEN_HEIGHT == 896.f)

#define TK_IS_iPhoneX_Series (TK_IS_iPhoneX_XS || TK_IS_iPhoneXR_XSMax)

#define TK_WEAK_SELF(weakSelf) __weak __typeof(&*self) weakSelf = self;
#define TK_STRONG_SELF(strongSelf) __strong __typeof(&*weakSelf) strongSelf = weakSelf;

// 计算单行大小
#define TK_TEXTSIZE(text, font) ([text length] > 0 ? [text sizeWithAttributes:@{NSFontAttributeName:font}] : CGSizeZero)
// 计算多行大小
#define TK_MULTILINE_TEXTSIZE(text, font, maxSize) (([text length] > 0) ? [text boundingRectWithSize:maxSize  \
options:(NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading) \
attributes:@{NSFontAttributeName:font} context:nil].size : CGSizeZero)
// 计算多行大小带行距
#define TK_MULTILINE_TEXTSIZE_FONTPARAGRAPH(text, font, paragraph, maxSize) ([text length] > 0 ? [text boundingRectWithSize:maxSize  \
options:(NSStringDrawingUsesLineFragmentOrigin | NSStringDrawingUsesFontLeading) \
attributes:@{NSFontAttributeName:font, NSParagraphStyleAttributeName:paragraph} context:nil].size : CGSizeZero)

#ifdef DEBUG
#define DLog( s, ... ) NSLog( @"<%@:(%d)>: %@", [[NSString stringWithUTF8String:__FILE__] lastPathComponent], __LINE__, [NSString stringWithFormat:(s), ##__VA_ARGS__] )
#else
#define DLog( s, ... )
#endif

#endif /* YdkToolkitMacros_h */
