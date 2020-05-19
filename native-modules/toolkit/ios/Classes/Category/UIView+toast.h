//
//  UIView+toast.h
//  ydk-toolkit
//
//  Created by yryz on 2019/7/4.
//

#import <UIKit/UIKit.h>

extern const NSString * YdkToastPositionTop;
extern const NSString * YdkToastPositionCenter;
extern const NSString * YdkToastPositionBottom;

@class YdkToastStyle;

@interface UIView (toast)

- (void)makeToast:(NSString *)message;

- (void)makeToast:(NSString *)message
         duration:(NSTimeInterval)duration
         position:(id)position;

- (void)makeToast:(NSString *)message
         duration:(NSTimeInterval)duration
         position:(id)position
            style:(YdkToastStyle *)style;

- (void)makeToast:(NSString *)message
         duration:(NSTimeInterval)duration
         position:(id)position
            title:(NSString *)title
            image:(UIImage *)image
            style:(YdkToastStyle *)style
       completion:(void(^)(BOOL didTap))completion;

- (UIView *)toastViewForMessage:(NSString *)message
                          title:(NSString *)title
                          image:(UIImage *)image
                          style:(YdkToastStyle *)style;

- (void)hideToasts;

- (void)hideToast:(UIView *)toast;

- (void)makeToastActivity:(id)position;

- (void)hideToastActivity;

- (void)showToast:(UIView *)toast;

- (void)showToast:(UIView *)toast
         duration:(NSTimeInterval)duration
         position:(id)position
       completion:(void(^)(BOOL didTap))completion;

@end

@interface YdkToastStyle : NSObject

/**
 The background color. Default is `[UIColor blackColor]` at 80% opacity.
 */
@property (strong, nonatomic) UIColor *backgroundColor;

/**
 The title color. Default is `[UIColor whiteColor]`.
 */
@property (strong, nonatomic) UIColor *titleColor;

/**
 The message color. Default is `[UIColor whiteColor]`.
 */
@property (strong, nonatomic) UIColor *messageColor;

/**
 A percentage value from 0.0 to 1.0, representing the maximum width of the toast
 view relative to it's superview. Default is 0.8 (80% of the superview's width).
 */
@property (assign, nonatomic) CGFloat maxWidthPercentage;

/**
 A percentage value from 0.0 to 1.0, representing the maximum height of the toast
 view relative to it's superview. Default is 0.8 (80% of the superview's height).
 */
@property (assign, nonatomic) CGFloat maxHeightPercentage;

/**
 The spacing from the horizontal edge of the toast view to the content. When an image
 is present, this is also used as the padding between the image and the text.
 Default is 10.0.
 */
@property (assign, nonatomic) CGFloat horizontalPadding;

/**
 The spacing from the vertical edge of the toast view to the content. When a title
 is present, this is also used as the padding between the title and the message.
 Default is 10.0.
 */
@property (assign, nonatomic) CGFloat verticalPadding;

/**
 The corner radius. Default is 10.0.
 */
@property (assign, nonatomic) CGFloat cornerRadius;

/**
 The title font. Default is `[UIFont boldSystemFontOfSize:16.0]`.
 */
@property (strong, nonatomic) UIFont *titleFont;

/**
 The message font. Default is `[UIFont systemFontOfSize:16.0]`.
 */
@property (strong, nonatomic) UIFont *messageFont;

/**
 The title text alignment. Default is `NSTextAlignmentLeft`.
 */
@property (assign, nonatomic) NSTextAlignment titleAlignment;

/**
 The message text alignment. Default is `NSTextAlignmentLeft`.
 */
@property (assign, nonatomic) NSTextAlignment messageAlignment;

/**
 The maximum number of lines for the title. The default is 0 (no limit).
 */
@property (assign, nonatomic) NSInteger titleNumberOfLines;

/**
 The maximum number of lines for the message. The default is 0 (no limit).
 */
@property (assign, nonatomic) NSInteger messageNumberOfLines;

/**
 Enable or disable a shadow on the toast view. Default is `NO`.
 */
@property (assign, nonatomic) BOOL displayShadow;

/**
 The shadow color. Default is `[UIColor blackColor]`.
 */
@property (strong, nonatomic) UIColor *shadowColor;

/**
 A value from 0.0 to 1.0, representing the opacity of the shadow.
 Default is 0.8 (80% opacity).
 */
@property (assign, nonatomic) CGFloat shadowOpacity;

/**
 The shadow radius. Default is 6.0.
 */
@property (assign, nonatomic) CGFloat shadowRadius;

/**
 The shadow offset. The default is `CGSizeMake(4.0, 4.0)`.
 */
@property (assign, nonatomic) CGSize shadowOffset;

/**
 The image size. The default is `CGSizeMake(80.0, 80.0)`.
 */
@property (assign, nonatomic) CGSize imageSize;

/**
 The size of the toast activity view when `makeToastActivity:` is called.
 Default is `CGSizeMake(100.0, 100.0)`.
 */
@property (assign, nonatomic) CGSize activitySize;

/**
 The fade in/out animation duration. Default is 0.2.
 */
@property (assign, nonatomic) NSTimeInterval fadeDuration;

/**
 Creates a new instance of `CSToastStyle` with all the default values set.
 */
- (instancetype)initWithDefaultStyle NS_DESIGNATED_INITIALIZER;

/**
 @warning Only the designated initializer should be used to create
 an instance of `CSToastStyle`.
 */
- (instancetype)init NS_UNAVAILABLE;

@end

/**
 `CSToastManager` provides general configuration options for all toast
 notifications. Backed by a singleton instance.
 */
@interface YdkToastManager : NSObject

/**
 Sets the shared style on the singleton. The shared style is used whenever
 a `makeToast:` method (or `toastViewForMessage:title:image:style:`) is called
 with with a nil style. By default, this is set to `CSToastStyle`'s default
 style.
 
 @param sharedStyle the shared style
 */
+ (void)setSharedStyle:(YdkToastStyle *)sharedStyle;

/**
 Gets the shared style from the singlton. By default, this is
 `CSToastStyle`'s default style.
 
 @return the shared style
 */
+ (YdkToastStyle *)sharedStyle;

/**
 Enables or disables tap to dismiss on toast views. Default is `YES`.
 
 @param tapToDismissEnabled YES or NO
 */
+ (void)setTapToDismissEnabled:(BOOL)tapToDismissEnabled;

/**
 Returns `YES` if tap to dismiss is enabled, otherwise `NO`.
 Default is `YES`.
 
 @return BOOL YES or NO
 */
+ (BOOL)isTapToDismissEnabled;

/**
 Enables or disables queueing behavior for toast views. When `YES`,
 toast views will appear one after the other. When `NO`, multiple Toast
 views will appear at the same time (potentially overlapping depending
 on their positions). This has no effect on the toast activity view,
 which operates independently of normal toast views. Default is `YES`.
 
 @param queueEnabled YES or NO
 */
+ (void)setQueueEnabled:(BOOL)queueEnabled;

/**
 Returns `YES` if the queue is enabled, otherwise `NO`.
 Default is `YES`.
 
 @return BOOL
 */
+ (BOOL)isQueueEnabled;

/**
 Sets the default duration. Used for the `makeToast:` and
 `showToast:` methods that don't require an explicit duration.
 Default is 3.0.
 
 @param duration The toast duration
 */
+ (void)setDefaultDuration:(NSTimeInterval)duration;

/**
 Returns the default duration. Default is 3.0.
 
 @return duration The toast duration
 */
+ (NSTimeInterval)defaultDuration;

/**
 Sets the default position. Used for the `makeToast:` and
 `showToast:` methods that don't require an explicit position.
 Default is `CSToastPositionBottom`.
 
 @param position The default center point. Can be one of the predefined
 CSToastPosition constants or a `CGPoint` wrapped in an `NSValue` object.
 */
+ (void)setDefaultPosition:(id)position;

/**
 Returns the default toast position. Default is `CSToastPositionBottom`.
 
 @return position The default center point. Will be one of the predefined
 CSToastPosition constants or a `CGPoint` wrapped in an `NSValue` object.
 */
+ (id)defaultPosition;

@end
