//
//  YdkKeychain.h
//  AFNetworking
//
//  Created by yryz on 2019/6/21.
//

#import <Foundation/Foundation.h>

@class YdkKeychainItem;

@interface YdkKeychain : NSObject

+ (NSString *)getPasswordForService:(NSString *)serviceName
                                     account:(NSString *)account
                                       error:(NSError **)error;
+ (NSString *)getPasswordForService:(NSString *)serviceName
                                     account:(NSString *)account;

+ (BOOL)deletePasswordForService:(NSString *)serviceName account:(NSString *)account error:(NSError **)error;
+ (BOOL)deletePasswordForService:(NSString *)serviceName account:(NSString *)account;

+ (BOOL)setPassword:(NSString *)password
         forService:(NSString *)serviceName
            account:(NSString *)account
              error:(NSError **)error;
+ (BOOL)setPassword:(NSString *)password
         forService:(NSString *)serviceName
            account:(NSString *)account;

+ (BOOL)insertItem:(YdkKeychainItem *)item error:(NSError **)error;
+ (BOOL)insertItem:(YdkKeychainItem *)item;

+ (BOOL)updateItem:(YdkKeychainItem *)item error:(NSError **)error;
+ (BOOL)updateItem:(YdkKeychainItem *)item;

+ (BOOL)deleteItem:(YdkKeychainItem *)item error:(NSError **)error;
+ (BOOL)deleteItem:(YdkKeychainItem *)item;

+ (YdkKeychainItem *)selectOneItem:(YdkKeychainItem *)item error:(NSError **)error;
+ (YdkKeychainItem *)selectOneItem:(YdkKeychainItem *)item;

+ (NSArray<YdkKeychainItem *> *)selectItems:(YdkKeychainItem *)item error:(NSError **)error;
+ (NSArray<YdkKeychainItem *> *)selectItems:(YdkKeychainItem *)item;

@end

typedef NS_ENUM (NSUInteger, YdkKeychainErrorCode) {
    YdkKeychainErrorUnimplemented = 1, ///< Function or operation not implemented.
    YdkKeychainErrorIO, ///< I/O error (bummers)
    YdkKeychainErrorOpWr, ///< File already open with with write permission.
    YdkKeychainErrorParam, ///< One or more parameters passed to a function where not valid.
    YdkKeychainErrorAllocate, ///< Failed to allocate memory.
    YdkKeychainErrorUserCancelled, ///< User cancelled the operation.
    YdkKeychainErrorBadReq, ///< Bad parameter or invalid state for operation.
    YdkKeychainErrorInternalComponent, ///< Internal...
    YdkKeychainErrorNotAvailable, ///< No keychain is available. You may need to restart your computer.
    YdkKeychainErrorDuplicateItem, ///< The specified item already exists in the keychain.
    YdkKeychainErrorItemNotFound, ///< The specified item could not be found in the keychain.
    YdkKeychainErrorInteractionNotAllowed, ///< User interaction is not allowed.
    YdkKeychainErrorDecode, ///< Unable to decode the provided data.
    YdkKeychainErrorAuthFailed, ///< The user name or passphrase you entered is not.
};


/**
 When query to return the item's data, the error
 errSecInteractionNotAllowed will be returned if the item's data is not
 available until a device unlock occurs.
 */
typedef NS_ENUM (NSUInteger, YdkKeychainAccessible) {
    YdkKeychainAccessibleNone = 0, ///< no value
    
    /** Item data can only be accessed
     while the device is unlocked. This is recommended for items that only
     need be accesible while the application is in the foreground.  Items
     with this attribute will migrate to a new device when using encrypted
     backups. */
    YdkKeychainAccessibleWhenUnlocked,
    
    /** Item data can only be
     accessed once the device has been unlocked after a restart.  This is
     recommended for items that need to be accesible by background
     applications. Items with this attribute will migrate to a new device
     when using encrypted backups.*/
    YdkKeychainAccessibleAfterFirstUnlock,
    
    /** Item data can always be accessed
     regardless of the lock state of the device.  This is not recommended
     for anything except system use. Items with this attribute will migrate
     to a new device when using encrypted backups.*/
    YdkKeychainAccessibleAlways,
    
    /** Item data can
     only be accessed while the device is unlocked. This class is only
     available if a passcode is set on the device. This is recommended for
     items that only need to be accessible while the application is in the
     foreground. Items with this attribute will never migrate to a new
     device, so after a backup is restored to a new device, these items
     will be missing. No items can be stored in this class on devices
     without a passcode. Disabling the device passcode will cause all
     items in this class to be deleted.*/
    YdkKeychainAccessibleWhenPasscodeSetThisDeviceOnly,
    
    /** Item data can only
     be accessed while the device is unlocked. This is recommended for items
     that only need be accesible while the application is in the foreground.
     Items with this attribute will never migrate to a new device, so after
     a backup is restored to a new device, these items will be missing. */
    YdkKeychainAccessibleWhenUnlockedThisDeviceOnly,
    
    /** Item data can
     only be accessed once the device has been unlocked after a restart.
     This is recommended for items that need to be accessible by background
     applications. Items with this attribute will never migrate to a new
     device, so after a backup is restored to a new device these items will
     be missing.*/
    YdkKeychainAccessibleAfterFirstUnlockThisDeviceOnly,
    
    /** Item data can always
     be accessed regardless of the lock state of the device.  This option
     is not recommended for anything except system use. Items with this
     attribute will never migrate to a new device, so after a backup is
     restored to a new device, these items will be missing.*/
    YdkKeychainAccessibleAlwaysThisDeviceOnly,
};

/**
 Whether the item in question can be synchronized.
 */
typedef NS_ENUM (NSUInteger, YdkKeychainQuerySynchronizationMode) {
    
    /** Default, Don't care for synchronization  */
    YdkKeychainQuerySynchronizationModeAny = 0,
    
    /** Is not synchronized */
    YdkKeychainQuerySynchronizationModeNo,
    
    /** To add a new item which can be synced to other devices, or to obtain
     synchronized results from a query*/
    YdkKeychainQuerySynchronizationModeYes,
} NS_AVAILABLE_IOS (7_0);



/**
 Wrapper for keychain item/query.
 */
@interface YdkKeychainItem : NSObject <NSCopying>

@property (nonatomic, copy) NSString *service; ///< kSecAttrService
@property (nonatomic, copy) NSString *account; ///< kSecAttrAccount
@property (nonatomic, copy) NSData *passwordData; ///< kSecValueData
@property (nonatomic, copy) NSString *password; ///< shortcut for `passwordData`
@property (nonatomic, copy) id <NSCoding> passwordObject; ///< shortcut for `passwordData`

@property (nonatomic, copy) NSString *label; ///< kSecAttrLabel
@property (nonatomic, copy) NSNumber *type; ///< kSecAttrType (FourCC)
@property (nonatomic, copy) NSNumber *creater; ///< kSecAttrCreator (FourCC)
@property (nonatomic, copy) NSString *comment; ///< kSecAttrComment
@property (nonatomic, copy) NSString *descr; ///< kSecAttrDescription
@property (nonatomic, readonly, strong) NSDate *modificationDate; ///< kSecAttrModificationDate
@property (nonatomic, readonly, strong) NSDate *creationDate; ///< kSecAttrCreationDate
@property (nonatomic, copy) NSString *accessGroup; ///< kSecAttrAccessGroup

@property (nonatomic) YdkKeychainAccessible accessible; ///< kSecAttrAccessible
@property (nonatomic) YdkKeychainQuerySynchronizationMode synchronizable NS_AVAILABLE_IOS(7_0); ///< kSecAttrSynchronizable

@end

