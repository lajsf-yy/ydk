
#import "YdkShare.h"
#import <ShareSDK/ShareSDK.h>
#import <ShareSDKConnector/ShareSDKConnector.h>
#import <ShareSDKExtension/ShareSDK+Extension.h>
#import <TencentOpenAPI/TencentOAuth.h>
#import <TencentOpenAPI/QQApiInterface.h>
#import <WechatConnector/WechatConnector.h>
#import "WXApi.h"
#import "WeiboSDK.h"

NSErrorDomain const YdkShareErrorDomain = @"YdkShareErrorDomain";

@interface YdkShareConfig: NSObject

@property (nonatomic, retain) NSString *sinaAppKey;
@property (nonatomic, retain)  NSString *sinaAppSecret;
@property (nonatomic, retain) NSString *sinaRedirectUrl;
@property (nonatomic, retain)  NSString *wechatAppId;
@property (nonatomic, retain)  NSString *wechatAppSecret;
@property (nonatomic, retain)   NSString *wechatMiniProgramerId;
@property (nonatomic, retain)   NSString *qqAppKey;
@property (nonatomic, retain)  NSString *qqAppId;
@property (nonatomic, retain)   NSString *universalLink;

@end

@implementation YdkShareConfig

@end

@implementation YdkShareData

@end

@interface YdkShare () <TencentSessionDelegate>

@property (nonatomic, strong) TencentOAuth *tencentOAuth;

@property (nonatomic, strong) YdkResolveBlock resolve;
@property (nonatomic, strong) YdkRejectBlock reject;

@end

@implementation YdkShare {
    NSDictionary* shareTypeDict;
    NSDictionary* contentTypeDict;
    YdkShareConfig* shareConfig;
}

+ (void)load {
    ydk_register_module(self);
}

- (instancetype)initWithConfig:(NSDictionary *)config {
    if (self = [super init]) {
        shareConfig = [config toObject:[YdkShareConfig class] prefix:@"share."];
        shareConfig.universalLink = [config objectForKey:@"universalLink"];
    }
    return self;
}

- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {
    shareTypeDict = @{
                      @"qq": @(SSDKPlatformSubTypeQQFriend),
                      @"qZone": @(SSDKPlatformSubTypeQZone),
                      @"weChat": @(SSDKPlatformSubTypeWechatSession),
                      @"weChatMoment": @(SSDKPlatformSubTypeWechatTimeline),
                      @"sinaWeibo": @(SSDKPlatformTypeSinaWeibo)
                      };
    contentTypeDict=@{ @"auto" : @(SSDKContentTypeAuto),
                       @"image" : @(SSDKContentTypeImage),
                       @"audio" : @(SSDKContentTypeAudio),
                       @"video" : @(SSDKContentTypeVideo) };
    [ShareSDK registPlatforms:^(SSDKRegister *platformsRegister) {
        [platformsRegister setupSinaWeiboWithAppkey:self->shareConfig.sinaAppKey appSecret:self->shareConfig.sinaAppSecret redirectUrl:self->shareConfig.sinaRedirectUrl];
        [platformsRegister setupWeChatWithAppId:self->shareConfig.wechatAppId appSecret:self->shareConfig.wechatAppSecret universalLink:self->shareConfig.universalLink];
        [platformsRegister setupQQWithAppId:self->shareConfig.qqAppId appkey:self->shareConfig.qqAppKey];
    }];
    return YES;
}

- (BOOL)application:(UIApplication *)application openURL:(NSURL *)url options:(NSDictionary<UIApplicationOpenURLOptionsKey,id> *)options {
    if (_tencentOAuth) [TencentOAuth HandleOpenURL:url];
    return YES;
}

-(SSDKPlatformType) getPlatformType:(NSString *)shareType{
    NSInteger type= [[self->shareTypeDict objectForKey:shareType] integerValue];
    return type;
}

-(SSDKContentType) getContentType:(NSString *)contentType{
    NSInteger type= [[self->contentTypeDict objectForKey:contentType] integerValue];
    return type;
}

- (void)share:(NSString *)platform shareData:(YdkShareData *)shareData
      resolve:(YdkResolveBlock)resolve reject:(YdkRejectBlock)reject{
    SSDKPlatformType platformType = [self getPlatformType:platform];
    SSDKContentType contentType = [self getContentType:shareData.contentType];;
    
    if(!platformType && reject){
        reject(enumToString(YdkShareErrorPlatformUnknown), @"未知的分享平台", [NSError errorWithDomain:YdkShareErrorDomain code:YdkShareErrorPlatformUnknown userInfo:@{NSLocalizedDescriptionKey : @"未知的分享平台"}]);
        return;
    }
    if(platformType==SSDKPlatformTypeSinaWeibo){
        shareData.content = [NSString stringWithFormat:@"%@%@",shareData.content,shareData.url];
    }
    
    NSURL *shareURL = shareData.url.length == 0 ? nil : [NSURL URLWithString:shareData.url];
    NSMutableDictionary *shareParams = [NSMutableDictionary dictionary];
    if (platformType == SSDKPlatformSubTypeWechatSession && shareData.path) {
        if(!shareData.thumbImage){
            shareData.thumbImage=shareData.imgUrl;
        }
        if(!shareData.hdThumbImage){
            shareData.thumbImage=shareData.thumbImage;
        }
        
        NSUInteger miniProgramType = [shareData.miniProgramType unsignedIntegerValue];
        NSString *userName = shareConfig. wechatMiniProgramerId;
        [shareParams SSDKSetupWeChatMiniProgramShareParamsByTitle:shareData.title
                                                      description:shareData.content
                                                       webpageUrl:shareURL
                                                             path:shareData.path
                                                       thumbImage:shareData.thumbImage
                                                     hdThumbImage:shareData.hdThumbImage
                                                         userName:userName
                                                  withShareTicket:YES
                                                  miniProgramType:miniProgramType
                                               forPlatformSubType:SSDKPlatformSubTypeWechatSession];
    } else {
        [shareParams SSDKSetupShareParamsByText:shareData.content
                                         images:@[shareData.imgUrl]
                                            url:shareURL
                                          title:shareData.title
                                           type:contentType];
    }
    [ShareSDK share:platformType parameters:shareParams onStateChanged:^(SSDKResponseState state, NSDictionary *userData, SSDKContentEntity *contentEntity, NSError *error) {
        switch (state) {
                case SSDKResponseStateSuccess: {
                    if (resolve) {
                        resolve(nil);
                    }
                }
                break;
                case SSDKResponseStateFail: {
                    if (reject) {
                        reject(enumToString(YdkShareErrorShareFailure) , @"分享失败", error);
                    }
                }
                break;
                case SSDKResponseStateCancel: {
                    if (reject) {
                        reject(YdkCancel, @"取消分享", nil);
                    }
                }
                break;
            default:
                break;
        }
    }];
}

- (void)authorizeLogin:(NSString*)platform resolve:(YdkResolveBlock)resolve reject:(YdkRejectBlock)reject {
    SSDKPlatformType platformType = [self getPlatformType:platform];
    if(!platformType){
        reject(enumToString(YdkShareErrorPlatformUnknown) , @"未知的第三方登录平台", [NSError errorWithDomain:YdkShareErrorDomain code:YdkShareErrorPlatformUnknown userInfo:@{NSLocalizedDescriptionKey : @"未知的第三方登录平台"}]);
        return;
    }
    [self _authorizeLogin:platformType resolve:resolve reject:reject];
}

- (void)authorize:(NSString*)platform resolve:(YdkResolveBlock)resolve reject:(YdkRejectBlock)reject {
    SSDKPlatformType platformType = [self getPlatformType:platform];
    if (platformType == SSDKPlatformSubTypeWechatSession) {
        [WeChatConnector setRequestAuthTokenOperation:^(NSString *authCode, void (^getUserinfo)(NSString *uid, NSString *token)) {
            if (resolve) {
                resolve(@{@"code" : authCode});
            }
        }];
        [self authorizeLogin:platform resolve:nil reject:reject];
        return;
    } else if (platformType == SSDKPlatformSubTypeQQFriend) {
        // NSArray* permissions = [NSArray arrayWithObjects:kOPEN_PERMISSION_GET_SIMPLE_USER_INFO,nil];
        _tencentOAuth = [[TencentOAuth alloc] initWithAppId:self->shareConfig.qqAppId andDelegate:self];
        _tencentOAuth.authMode = kAuthModeServerSideCode;
        [_tencentOAuth authorize:nil inSafari:NO];
        _resolve = resolve;
        _reject = reject;
        return;
    }
    
    if (reject) {
        NSError *error = [NSError errorWithDomain:YdkShareErrorDomain code:YdkShareErrorLoginFailure userInfo:@{ NSLocalizedDescriptionKey : @"不支持的授权平台类型" }];
        reject(enumToString(YdkShareErrorNotSupportAuthorizePlatform), @"授权失败", error);
    }
}

- (void)_authorizeLogin:(SSDKPlatformType)platform resolve:(YdkResolveBlock)resolve reject:(YdkRejectBlock)reject {
    NSDictionary *settings = nil;
    [ShareSDK authorize:platform settings:settings onStateChanged:^(SSDKResponseState state, SSDKUser *user, NSError *error) {
        switch (state) {
            case SSDKResponseStateSuccess: {
                if (resolve) {
                    NSString *userId = user.credential.uid ? : @"";
                    NSString *token = user.credential.token ? : @"";
                    NSString *userGender = @(user.gender).stringValue;
                    NSString *userIcon = user.icon ? : @"";
                    NSString *userName = user.nickname ? : @"";
                    NSDictionary *info = @{ @"token" : token, @"userId" : userId, @"userGender": userGender, @"userIcon": userIcon, @"userName": userName };
                    resolve(info);
                }
            }
                break;
            case SSDKResponseStateFail: {
                if (reject) {
                    reject(enumToString(YdkShareErrorLoginFailure), @"登录失败", error);
                }
            }
                break;
            case SSDKResponseStateCancel: {
                if (reject) {
                    reject(YdkCancel, @"取消登录", nil);
                }
            }
                break;
            default:
                break;
        }
    }];
}

- (void)getInstallPlatforms:(YdkResolveBlock) resolve reject:(YdkRejectBlock)reject{
    NSMutableArray *platforms = [NSMutableArray array];
    
    NSArray *array = @[@(SSDKPlatformSubTypeQQFriend),
                       @(SSDKPlatformSubTypeWechatSession),
                       @(SSDKPlatformTypeSinaWeibo)];
    
    dispatch_async(dispatch_get_main_queue(), ^{
        [array enumerateObjectsUsingBlock:^(id  _Nonnull obj, NSUInteger idx, BOOL * _Nonnull stop) {
            if ([ShareSDK isClientInstalled:[obj integerValue] ]) {
                NSString* platformName = [self->shareTypeDict getKeyFromValue:obj];
                [platforms addObject:platformName];
            }
        }];
        resolve(platforms);
    });
}

// MARK: - Public Method
+ (void)share:(NSString *)platform shareData:(YdkShareData *)shareData resolve:(YdkResolveBlock)resolve reject:(YdkRejectBlock)reject {
    YdkShare *module = ydk_get_module_instance(self.class);
    [module share:platform shareData:shareData resolve:resolve reject:reject];
}

+ (void)authorizeLogin:(NSString*)platform resolve:(YdkResolveBlock)resolve reject:(YdkRejectBlock)reject {
    YdkShare *module = ydk_get_module_instance(self.class);
    [module authorizeLogin:platform resolve:resolve reject:reject];
}

+ (void)authorize:(NSString*)platform resolve:(YdkResolveBlock)resolve reject:(YdkRejectBlock)reject {
    YdkShare *module = ydk_get_module_instance(self.class);
    [module authorize:platform resolve:resolve reject:reject];
}

+ (void)getInstallPlatforms:(YdkResolveBlock)resolve reject:(YdkRejectBlock)reject {
    YdkShare *module = ydk_get_module_instance(self.class);
    [module getInstallPlatforms:resolve reject:reject];
}

// MARK: - TencentSessionDelegate

/**
 * [该逻辑未实现]因token失效而需要执行重新登录授权。在用户调用某个api接口时，如果服务器返回token失效，则触发该回调协议接口，由第三方决定是否跳转到登录授权页面，让用户重新授权。
 * \param tencentOAuth 登录授权对象。
 * \return 是否仍然回调返回原始的api请求结果。
 * \note 不实现该协议接口则默认为不开启重新登录授权流程。若需要重新登录授权请调用\ref TencentOAuth#reauthorizeWithPermissions: \n注意：重新登录授权时用户可能会修改登录的帐号
 */
- (BOOL)tencentNeedPerformReAuth:(TencentOAuth *)tencentOAuth {
    return YES;
}

/**
 * 用户增量授权过程中因取消或网络问题导致授权失败
 * \param reason 授权失败原因，具体失败原因参见sdkdef.h文件中\ref UpdateFailType
 */
- (void)tencentFailedUpdate:(UpdateFailType)reason {
    if (_reject) {
        if (reason == kUpdateFailUserCancel) {
            _reject(YdkCancel, @"取消分享", nil);
        } else {
            _reject(enumToString(YdkShareErrorAuthorizeFailed), @"授权失败", [NSError errorWithDomain:YdkShareErrorDomain code:YdkShareErrorAuthorizeFailed userInfo:@{NSLocalizedDescriptionKey : @"未知的分享平台"}]);
        }
        [self clean];
    }
}

/**
 * 因用户未授予相应权限而需要执行增量授权。在用户调用某个api接口时，如果服务器返回操作未被授权，则触发该回调协议接口，由第三方决定是否跳转到增量授权页面，让用户重新授权。
 * \param tencentOAuth 登录授权对象。
 * \param permissions 需增量授权的权限列表。
 * \return 是否仍然回调返回原始的api请求结果。
 * \note 不实现该协议接口则默认为不开启增量授权流程。若需要增量授权请调用\ref TencentOAuth#incrAuthWithPermissions: \n注意：增量授权时用户可能会修改登录的帐号
 */
- (BOOL)tencentNeedPerformIncrAuth:(TencentOAuth *)tencentOAuth withPermissions:(NSArray *)permissions {
    return YES;
}

/**
 * 用户通过增量授权流程重新授权登录，token及有效期限等信息已被更新。
 * \param tencentOAuth token及有效期限等信息更新后的授权实例对象
 * \note 第三方应用需更新已保存的token及有效期限等信息。
 */
- (void)tencentDidUpdate:(TencentOAuth *)tencentOAuth {
    
}

/**
 * 通知第三方界面需要被关闭
 * \param tencentOAuth 返回回调的tencentOAuth对象
 * \param viewController 需要关闭的viewController
 */
- (void)tencentOAuth:(TencentOAuth *)tencentOAuth doCloseViewController:(UIViewController *)viewController {
    
}

- (void)tencentDidLogin {
    if (_resolve) {
        _resolve(@{@"code" : [_tencentOAuth getServerSideCode] ? : @""});
        [self clean];
    }
}

- (void)tencentDidNotLogin:(BOOL)cancelled {
    if (_reject) {
        if (cancelled) {
            _reject(YdkCancel, @"取消分享", nil);
        } else {
            _reject(enumToString(YdkShareErrorLoginFailure), @"登录失败", [NSError errorWithDomain:YdkShareErrorDomain code:YdkShareErrorLoginFailure userInfo:@{NSLocalizedDescriptionKey : @"登录失败"}]);
        }
        [self clean];
    }
}

/**
 * 登录时网络有问题的回调
 */
- (void)tencentDidNotNetWork {
    
}

/**
 * 登录时权限信息的获得
 */
- (NSArray *)getAuthorizedPermissions:(NSArray *)permissions withExtraParams:(NSDictionary *)extraParams {
    return nil;
}

- (void)clean {
    _tencentOAuth = nil;
    _reject = nil;
    _resolve = nil;
}

@end



