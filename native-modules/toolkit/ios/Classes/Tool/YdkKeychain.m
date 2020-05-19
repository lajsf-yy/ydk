//
//  YdkKeychain.m
//  AFNetworking
//
//  Created by yryz on 2019/6/21.
//

#import "YdkKeychain.h"
#import <Security/Security.h>

static YdkKeychainErrorCode YdkKeychainErrorCodeFromOSStatus(OSStatus status) {
    switch (status) {
            case errSecUnimplemented: return YdkKeychainErrorUnimplemented;
            case errSecIO: return YdkKeychainErrorIO;
            case errSecOpWr: return YdkKeychainErrorOpWr;
            case errSecParam: return YdkKeychainErrorParam;
            case errSecAllocate: return YdkKeychainErrorAllocate;
            case errSecUserCanceled: return YdkKeychainErrorUserCancelled;
            case errSecBadReq: return YdkKeychainErrorBadReq;
            case errSecInternalComponent: return YdkKeychainErrorInternalComponent;
            case errSecNotAvailable: return YdkKeychainErrorNotAvailable;
            case errSecDuplicateItem: return YdkKeychainErrorDuplicateItem;
            case errSecItemNotFound: return YdkKeychainErrorItemNotFound;
            case errSecInteractionNotAllowed: return YdkKeychainErrorInteractionNotAllowed;
            case errSecDecode: return YdkKeychainErrorDecode;
            case errSecAuthFailed: return YdkKeychainErrorAuthFailed;
        default: return 0;
    }
}

static NSString *YdkKeychainErrorDesc(YdkKeychainErrorCode code) {
    switch (code) {
            case YdkKeychainErrorUnimplemented:
            return @"Function or operation not implemented.";
            case YdkKeychainErrorIO:
            return @"I/O error (bummers)";
            case YdkKeychainErrorOpWr:
            return @"ile already open with with write permission.";
            case YdkKeychainErrorParam:
            return @"One or more parameters passed to a function where not valid.";
            case YdkKeychainErrorAllocate:
            return @"Failed to allocate memory.";
            case YdkKeychainErrorUserCancelled:
            return @"User canceled the operation.";
            case YdkKeychainErrorBadReq:
            return @"Bad parameter or invalid state for operation.";
            case YdkKeychainErrorInternalComponent:
            return @"Inrernal Component";
            case YdkKeychainErrorNotAvailable:
            return @"No keychain is available. You may need to restart your computer.";
            case YdkKeychainErrorDuplicateItem:
            return @"The specified item already exists in the keychain.";
            case YdkKeychainErrorItemNotFound:
            return @"The specified item could not be found in the keychain.";
            case YdkKeychainErrorInteractionNotAllowed:
            return @"User interaction is not allowed.";
            case YdkKeychainErrorDecode:
            return @"Unable to decode the provided data.";
            case YdkKeychainErrorAuthFailed:
            return @"The user name or passphrase you entered is not";
        default:
            break;
    }
    return nil;
}

static NSString *YdkKeychainAccessibleStr(YdkKeychainAccessible e) {
    switch (e) {
            case YdkKeychainAccessibleWhenUnlocked:
            return (__bridge NSString *)(kSecAttrAccessibleWhenUnlocked);
            case YdkKeychainAccessibleAfterFirstUnlock:
            return (__bridge NSString *)(kSecAttrAccessibleAfterFirstUnlock);
            case YdkKeychainAccessibleAlways:
            return (__bridge NSString *)(kSecAttrAccessibleAlways);
            case YdkKeychainAccessibleWhenPasscodeSetThisDeviceOnly:
            return (__bridge NSString *)(kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly);
            case YdkKeychainAccessibleWhenUnlockedThisDeviceOnly:
            return (__bridge NSString *)(kSecAttrAccessibleWhenUnlockedThisDeviceOnly);
            case YdkKeychainAccessibleAfterFirstUnlockThisDeviceOnly:
            return (__bridge NSString *)(kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly);
            case YdkKeychainAccessibleAlwaysThisDeviceOnly:
            return (__bridge NSString *)(kSecAttrAccessibleAlwaysThisDeviceOnly);
        default:
            return nil;
    }
}

static YdkKeychainAccessible YdkKeychainAccessibleEnum(NSString *s) {
    if ([s isEqualToString:(__bridge NSString *)kSecAttrAccessibleWhenUnlocked])
    return YdkKeychainAccessibleWhenUnlocked;
    if ([s isEqualToString:(__bridge NSString *)kSecAttrAccessibleAfterFirstUnlock])
    return YdkKeychainAccessibleAfterFirstUnlock;
    if ([s isEqualToString:(__bridge NSString *)kSecAttrAccessibleAlways])
    return YdkKeychainAccessibleAlways;
    if ([s isEqualToString:(__bridge NSString *)kSecAttrAccessibleWhenPasscodeSetThisDeviceOnly])
    return YdkKeychainAccessibleWhenPasscodeSetThisDeviceOnly;
    if ([s isEqualToString:(__bridge NSString *)kSecAttrAccessibleWhenUnlockedThisDeviceOnly])
    return YdkKeychainAccessibleWhenUnlockedThisDeviceOnly;
    if ([s isEqualToString:(__bridge NSString *)kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly])
    return YdkKeychainAccessibleAfterFirstUnlockThisDeviceOnly;
    if ([s isEqualToString:(__bridge NSString *)kSecAttrAccessibleAlwaysThisDeviceOnly])
    return YdkKeychainAccessibleAlwaysThisDeviceOnly;
    return YdkKeychainAccessibleNone;
}

static id YdkKeychainQuerySynchonizationID(YdkKeychainQuerySynchronizationMode mode) {
    switch (mode) {
            case YdkKeychainQuerySynchronizationModeAny:
            return (__bridge id)(kSecAttrSynchronizableAny);
            case YdkKeychainQuerySynchronizationModeNo:
            return (__bridge id)kCFBooleanFalse;
            case YdkKeychainQuerySynchronizationModeYes:
            return (__bridge id)kCFBooleanTrue;
        default:
            return (__bridge id)(kSecAttrSynchronizableAny);
    }
}

static YdkKeychainQuerySynchronizationMode YdkKeychainQuerySynchonizationEnum(NSNumber *num) {
    if ([num isEqualToNumber:@NO]) return YdkKeychainQuerySynchronizationModeNo;
    if ([num isEqualToNumber:@YES]) return YdkKeychainQuerySynchronizationModeYes;
    return YdkKeychainQuerySynchronizationModeAny;
}

@interface YdkKeychainItem ()
@property (nonatomic, readwrite, strong) NSDate *modificationDate;
@property (nonatomic, readwrite, strong) NSDate *creationDate;
@end

@implementation YdkKeychainItem


- (void)setPasswordObject:(id <NSCoding> )object {
    self.passwordData = [NSKeyedArchiver archivedDataWithRootObject:object];
}

- (id <NSCoding> )passwordObject {
    if ([self.passwordData length]) {
        return [NSKeyedUnarchiver unarchiveObjectWithData:self.passwordData];
    }
    return nil;
}

- (void)setPassword:(NSString *)password {
    self.passwordData = [password dataUsingEncoding:NSUTF8StringEncoding];
}

- (NSString *)password {
    if ([self.passwordData length]) {
        return [[NSString alloc] initWithData:self.passwordData encoding:NSUTF8StringEncoding];
    }
    return nil;
}

- (NSMutableDictionary *)queryDic {
    NSMutableDictionary *dic = [NSMutableDictionary new];
    
    dic[(__bridge id)kSecClass] = (__bridge id)kSecClassGenericPassword;
    
    if (self.account) dic[(__bridge id)kSecAttrAccount] = self.account;
    if (self.service) dic[(__bridge id)kSecAttrService] = self.service;
    
#if TARGET_OS_SIMULATOR
    
#else
    if (self.accessGroup) dic[(__bridge id)kSecAttrAccessGroup] = self.accessGroup;
#endif
    dic[(__bridge id)kSecAttrSynchronizable] = YdkKeychainQuerySynchonizationID(self.synchronizable);
    
    return dic;
}

- (NSMutableDictionary *)dic {
    NSMutableDictionary *dic = [NSMutableDictionary new];
    
    dic[(__bridge id)kSecClass] = (__bridge id)kSecClassGenericPassword;
    
    if (self.account) dic[(__bridge id)kSecAttrAccount] = self.account;
    if (self.service) dic[(__bridge id)kSecAttrService] = self.service;
    if (self.label) dic[(__bridge id)kSecAttrLabel] = self.label;

#if TARGET_OS_SIMULATOR
    
#else
    if (self.accessGroup) dic[(__bridge id)kSecAttrAccessGroup] = self.accessGroup;
#endif
    
    dic[(__bridge id)kSecAttrSynchronizable] = YdkKeychainQuerySynchonizationID(self.synchronizable);
    
    if (self.accessible) dic[(__bridge id)kSecAttrAccessible] = YdkKeychainAccessibleStr(self.accessible);
    if (self.passwordData) dic[(__bridge id)kSecValueData] = self.passwordData;
    if (self.type) dic[(__bridge id)kSecAttrType] = self.type;
    if (self.creater) dic[(__bridge id)kSecAttrCreator] = self.creater;
    if (self.comment) dic[(__bridge id)kSecAttrComment] = self.comment;
    if (self.descr) dic[(__bridge id)kSecAttrDescription] = self.descr;
    
    return dic;
}

- (instancetype)initWithDic:(NSDictionary *)dic {
    if (dic.count == 0) return nil;
    self = self.init;
    
    self.service = dic[(__bridge id)kSecAttrService];
    self.account = dic[(__bridge id)kSecAttrAccount];
    self.passwordData = dic[(__bridge id)kSecValueData];
    self.label = dic[(__bridge id)kSecAttrLabel];
    self.type = dic[(__bridge id)kSecAttrType];
    self.creater = dic[(__bridge id)kSecAttrCreator];
    self.comment = dic[(__bridge id)kSecAttrComment];
    self.descr = dic[(__bridge id)kSecAttrDescription];
    self.modificationDate = dic[(__bridge id)kSecAttrModificationDate];
    self.creationDate = dic[(__bridge id)kSecAttrCreationDate];
    self.accessGroup = dic[(__bridge id)kSecAttrAccessGroup];
    self.accessible = YdkKeychainAccessibleEnum(dic[(__bridge id)kSecAttrAccessible]);
    self.synchronizable = YdkKeychainQuerySynchonizationEnum(dic[(__bridge id)kSecAttrSynchronizable]);
    
    return self;
}

- (id)copyWithZone:(NSZone *)zone {
    YdkKeychainItem *item = [YdkKeychainItem new];
    item.service = self.service;
    item.account = self.account;
    item.passwordData = self.passwordData;
    item.label = self.label;
    item.type = self.type;
    item.creater = self.creater;
    item.comment = self.comment;
    item.descr = self.descr;
    item.modificationDate = self.modificationDate;
    item.creationDate = self.creationDate;
    item.accessGroup = self.accessGroup;
    item.accessible = self.accessible;
    item.synchronizable = self.synchronizable;
    return item;
}

- (NSString *)description {
    NSMutableString *str = @"".mutableCopy;
    [str appendString:@"YdkKeychainItem:{\n"];
    if (self.service) [str appendFormat:@"  service:%@,\n", self.service];
    if (self.account) [str appendFormat:@"  service:%@,\n", self.account];
    if (self.password) [str appendFormat:@"  service:%@,\n", self.password];
    if (self.label) [str appendFormat:@"  service:%@,\n", self.label];
    if (self.type) [str appendFormat:@"  service:%@,\n", self.type];
    if (self.creater) [str appendFormat:@"  service:%@,\n", self.creater];
    if (self.comment) [str appendFormat:@"  service:%@,\n", self.comment];
    if (self.descr) [str appendFormat:@"  service:%@,\n", self.descr];
    if (self.modificationDate) [str appendFormat:@"  service:%@,\n", self.modificationDate];
    if (self.creationDate) [str appendFormat:@"  service:%@,\n", self.creationDate];
    if (self.accessGroup) [str appendFormat:@"  service:%@,\n", self.accessGroup];
    [str appendString:@"}"];
    return str;
}

@end



@implementation YdkKeychain

+ (NSString *)getPasswordForService:(NSString *)serviceName
                            account:(NSString *)account
                              error:(NSError **)error {
    if (!serviceName || !account) {
        if (error) *error = [YdkKeychain errorWithCode:errSecParam];
        return nil;
    }
    
    YdkKeychainItem *item = [YdkKeychainItem new];
    item.service = serviceName;
    item.account = account;
    YdkKeychainItem *result = [self selectOneItem:item error:error];
    return result.password;
}

+ (nullable NSString *)getPasswordForService:(NSString *)serviceName
                                     account:(NSString *)account {
    return [self getPasswordForService:serviceName account:account error:NULL];
}

+ (BOOL)deletePasswordForService:(NSString *)serviceName
                         account:(NSString *)account
                           error:(NSError **)error {
    if (!serviceName || !account) {
        if (error) *error = [YdkKeychain errorWithCode:errSecParam];
        return NO;
    }
    
    YdkKeychainItem *item = [YdkKeychainItem new];
    item.service = serviceName;
    item.account = account;
    return [self deleteItem:item error:error];
}

+ (BOOL)deletePasswordForService:(NSString *)serviceName account:(NSString *)account {
    return [self deletePasswordForService:serviceName account:account error:NULL];
}

+ (BOOL)setPassword:(NSString *)password
         forService:(NSString *)serviceName
            account:(NSString *)account
              error:(NSError **)error {
    if (!password || !serviceName || !account) {
        if (error) *error = [YdkKeychain errorWithCode:errSecParam];
        return NO;
    }
    YdkKeychainItem *item = [YdkKeychainItem new];
    item.service = serviceName;
    item.account = account;
    YdkKeychainItem *result = [self selectOneItem:item error:NULL];
    if (result) {
        result.password = password;
        return [self updateItem:result error:error];
    } else {
        item.password = password;
        return [self insertItem:item error:error];
    }
}

+ (BOOL)setPassword:(NSString *)password
         forService:(NSString *)serviceName
            account:(NSString *)account {
    return [self setPassword:password forService:serviceName account:account error:NULL];
}

+ (BOOL)insertItem:(YdkKeychainItem *)item error:(NSError **)error {
    if (!item.service || !item.account || !item.passwordData) {
        if (error) *error = [YdkKeychain errorWithCode:errSecParam];
        return NO;
    }
    
    NSMutableDictionary *query = [item dic];
    OSStatus status = status = SecItemAdd((__bridge CFDictionaryRef)query, NULL);
    if (status != errSecSuccess) {
        if (error) *error = [YdkKeychain errorWithCode:status];
        return NO;
    }
    
    return YES;
}

+ (BOOL)insertItem:(YdkKeychainItem *)item {
    return [self insertItem:item error:NULL];
}

+ (BOOL)updateItem:(YdkKeychainItem *)item error:(NSError **)error {
    if (!item.service || !item.account || !item.passwordData) {
        if (error) *error = [YdkKeychain errorWithCode:errSecParam];
        return NO;
    }
    
    NSMutableDictionary *query = [item queryDic];
    NSMutableDictionary *update = [item dic];
    [update removeObjectForKey:(__bridge id)kSecClass];
    if (!query || !update) return NO;
    OSStatus status = status = SecItemUpdate((__bridge CFDictionaryRef)query, (__bridge CFDictionaryRef)update);
    if (status != errSecSuccess) {
        if (error) *error = [YdkKeychain errorWithCode:status];
        return NO;
    }
    
    return YES;
}

+ (BOOL)updateItem:(YdkKeychainItem *)item {
    return [self updateItem:item error:NULL];
}

+ (BOOL)deleteItem:(YdkKeychainItem *)item error:(NSError **)error {
    if (!item.service || !item.account) {
        if (error) *error = [YdkKeychain errorWithCode:errSecParam];
        return NO;
    }
    
    NSMutableDictionary *query = [item dic];
    OSStatus status = SecItemDelete((__bridge CFDictionaryRef)query);
    if (status != errSecSuccess) {
        if (error) *error = [YdkKeychain errorWithCode:status];
        return NO;
    }
    
    return YES;
}

+ (BOOL)deleteItem:(YdkKeychainItem *)item {
    return [self deleteItem:item error:NULL];
}

+ (YdkKeychainItem *)selectOneItem:(YdkKeychainItem *)item error:(NSError **)error {
    if (!item.service || !item.account) {
        if (error) *error = [YdkKeychain errorWithCode:errSecParam];
        return nil;
    }
    
    NSMutableDictionary *query = [item dic];
    query[(__bridge id)kSecMatchLimit] = (__bridge id)kSecMatchLimitOne;
    query[(__bridge id)kSecReturnAttributes] = @YES;
    query[(__bridge id)kSecReturnData] = @YES;
    
    OSStatus status;
    CFTypeRef result = NULL;
    status = SecItemCopyMatching((__bridge CFDictionaryRef)query, &result);
    if (status != errSecSuccess) {
        if (error) *error = [[self class] errorWithCode:status];
        return nil;
    }
    if (!result) return nil;
    
    NSDictionary *dic = nil;
    if (CFGetTypeID(result) == CFDictionaryGetTypeID()) {
        dic = (__bridge NSDictionary *)(result);
    } else if (CFGetTypeID(result) == CFArrayGetTypeID()){
        dic = [(__bridge NSArray *)(result) firstObject];
        if (![dic isKindOfClass:[NSDictionary class]]) dic = nil;
    }
    if (!dic.count) return nil;
    return [[YdkKeychainItem alloc] initWithDic:dic];
}

+ (YdkKeychainItem *)selectOneItem:(YdkKeychainItem *)item {
    return [self selectOneItem:item error:NULL];
}

+ (NSArray *)selectItems:(YdkKeychainItem *)item error:(NSError **)error {
    NSMutableDictionary *query = [item dic];
    query[(__bridge id)kSecMatchLimit] = (__bridge id)kSecMatchLimitAll;
    query[(__bridge id)kSecReturnAttributes] = @YES;
    query[(__bridge id)kSecReturnData] = @YES;
    
    OSStatus status;
    CFTypeRef result = NULL;
    status = SecItemCopyMatching((__bridge CFDictionaryRef)query, &result);
    if (status != errSecSuccess && error != NULL) {
        *error = [[self class] errorWithCode:status];
        return nil;
    }
    
    NSMutableArray *res = [NSMutableArray new];
    NSDictionary *dic = nil;
    if (CFGetTypeID(result) == CFDictionaryGetTypeID()) {
        dic = (__bridge NSDictionary *)(result);
        YdkKeychainItem *item = [[YdkKeychainItem alloc] initWithDic:dic];
        if (item) [res addObject:item];
    } else if (CFGetTypeID(result) == CFArrayGetTypeID()){
        for (NSDictionary *dic in (__bridge NSArray *)(result)) {
            YdkKeychainItem *item = [[YdkKeychainItem alloc] initWithDic:dic];
            if (item) [res addObject:item];
        }
    }
    
    return res;
}

+ (NSArray *)selectItems:(YdkKeychainItem *)item {
    return [self selectItems:item error:NULL];
}

+ (NSError *)errorWithCode:(OSStatus)osCode {
    YdkKeychainErrorCode code = YdkKeychainErrorCodeFromOSStatus(osCode);
    NSString *desc = YdkKeychainErrorDesc(code);
    NSDictionary *userInfo = desc ? @{ NSLocalizedDescriptionKey : desc } : nil;
    return [NSError errorWithDomain:@"com.yryz.ydk" code:code userInfo:userInfo];
}

@end
