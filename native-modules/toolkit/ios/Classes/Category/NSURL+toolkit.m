//
//  NSURL+toolkit.m
//  ydk-toolkit
//
//  Created by yryz on 2019/6/21.
//

#import "NSURL+toolkit.h"
#import <MobileCoreServices/MobileCoreServices.h>

// C Functions
BOOL tk_extensionToLikelyUTIMatch(NSString *extension, CFStringRef theUTI) {
    NSString *preferredUTI = (__bridge_transfer NSString *)UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, (__bridge CFStringRef)(extension), NULL);
    // 顺应树
    return (UTTypeConformsTo((__bridge CFStringRef) preferredUTI, theUTI));
}

BOOL tk_pathExtensionToLikelyVideo(NSString *extension) {
    return tk_extensionToLikelyUTIMatch(extension, CFSTR("public.movie"));
}

BOOL tk_pathExtensionToLikelyImage(NSString *extension) {
    return tk_extensionToLikelyUTIMatch(extension, CFSTR("public.image"));
}

BOOL tk_pathExtensionToLikelyAudio(NSString *extension) {
    return tk_extensionToLikelyUTIMatch(extension, CFSTR("public.audio"));
}

NSString* tk_preferredUTIForExtention(NSString *extension) {
    NSString *UTI = (__bridge_transfer NSString *)UTTypeCreatePreferredIdentifierForTag(kUTTagClassFilenameExtension, (__bridge CFStringRef)(extension), NULL);
    return UTI;
}

@implementation NSURL (toolkit)

- (BOOL)tk_isImageFile {
    return tk_pathExtensionToLikelyImage(self.pathExtension);
}

- (BOOL)tk_isVideoFile {
    return tk_pathExtensionToLikelyVideo(self.pathExtension);
}

- (BOOL)tk_isAudioFile {
    return tk_pathExtensionToLikelyAudio(self.pathExtension);
}

- (NSString *)tk_UTI {
    return tk_preferredUTIForExtention(self.pathExtension);
}

@end
