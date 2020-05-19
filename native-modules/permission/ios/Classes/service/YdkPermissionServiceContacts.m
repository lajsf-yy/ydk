//
//  YdkPermissionServiceContacts.m
//  ydk-permission
//
//  Created by yryz on 2019/7/11.
//

#import "YdkPermissionServiceContacts.h"
#import <AddressBook/AddressBook.h>
#import <Contacts/Contacts.h>

@implementation YdkPermissionServiceContacts

// 1. 获取权限状态
- (YdkPermissionAuthorizationStatus)permissionAuthorizationStatus {
    if (@available(iOS 9.0, *)) {
        CNAuthorizationStatus status = [CNContactStore authorizationStatusForEntityType:CNEntityTypeContacts];
        return [self permissionStatusWithCNAuthorizationStatus:status];
        
    } else {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
        ABAuthorizationStatus status = ABAddressBookGetAuthorizationStatus();
#pragma clang diagnostic pop
        return [self permissionStatusWithABAuthorizationStatus:status];
    }
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
- (YdkPermissionAuthorizationStatus)permissionStatusWithABAuthorizationStatus:(ABAuthorizationStatus)status {
    switch (status) {
        case kABAuthorizationStatusNotDetermined:
            return YdkPermissionAuthorizationStatusNotDetermined;
            break;
        case kABAuthorizationStatusRestricted:
            return YdkPermissionAuthorizationStatusRestricted;
            break;
        case kABAuthorizationStatusDenied:
            return YdkPermissionAuthorizationStatusDenied;
            break;
        case kABAuthorizationStatusAuthorized:
            return YdkPermissionAuthorizationStatusAuthorized;
            break;
    }
}
#pragma clang diagnostic pop

- (YdkPermissionAuthorizationStatus)permissionStatusWithCNAuthorizationStatus:(CNAuthorizationStatus)status API_AVAILABLE(ios(9.0)) {
    switch (status) {
        case CNAuthorizationStatusNotDetermined:
            return YdkPermissionAuthorizationStatusNotDetermined;
            break;
        case CNAuthorizationStatusRestricted:
            return YdkPermissionAuthorizationStatusRestricted;
            break;
        case CNAuthorizationStatusDenied:
            return YdkPermissionAuthorizationStatusDenied;
            break;
        case CNAuthorizationStatusAuthorized:
            return YdkPermissionAuthorizationStatusAuthorized;
            break;
    }
}

// 2. 请求权限并返回权限状态
- (RACSignal<NSNumber/*PermissionAuthorizationStatus*/ *> *)requestAuthorization {
    if (@available(iOS 9.0, *)) {
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            CNContactStore *store = [[CNContactStore alloc] init];
            [store requestAccessForEntityType:CNEntityTypeContacts completionHandler:^(BOOL granted, NSError *error) {
                if (error) {
                    [subscriber sendError:error];
                } else {
                    [subscriber sendNext:@(granted ? YdkPermissionAuthorizationStatusAuthorized : YdkPermissionAuthorizationStatusDenied)];
                    [subscriber sendCompleted];
                }
            }];
            return nil;
        }];
    } else {
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wdeprecated-declarations"
        return [RACSignal createSignal:^RACDisposable *(id<RACSubscriber> subscriber) {
            ABAddressBookRef addressBook = ABAddressBookCreateWithOptions(nil, nil);
            ABAddressBookRequestAccessWithCompletion(addressBook, ^(bool granted, CFErrorRef cfError) {
                NSError *error = (__bridge NSError *)cfError;
                if (error) {
                    [subscriber sendError:error];
                } else {
                    [subscriber sendNext:@(granted ? YdkPermissionAuthorizationStatusAuthorized : YdkPermissionAuthorizationStatusDenied)];
                    [subscriber sendCompleted];
                }
            });
            return nil;
        }];
#pragma clang diagnostic pop
    }
}

@end
