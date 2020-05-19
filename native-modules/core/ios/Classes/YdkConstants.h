
FOUNDATION_EXPORT NSString *const YdkCancel;

/**
 * 成功回调
 */
typedef void (^YdkResolveBlock)(id result);

/**
 * 失败回调
 */
typedef void (^YdkRejectBlock)(NSString * code, NSString *message, NSError *error);
#define enumToString(value)  @#value
