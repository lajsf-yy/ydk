//
//  YdkNetworkModule.m
//  ydk-network
//
//  Created by yryz on 2019/6/19.
//

#import "YdkAlbumModule.h"
#import "YdkAlbumPickerConfig.h"
#import "YdkVideoInfo.h"
#import "YdkAlbum.h"

#import <React/RCTBridge.h>
#import <React/RCTImageLoader.h>
#import <ydk-toolkit/YdkToolkit.h>

@implementation YdkAlbumModule

@synthesize bridge = _bridge;

RCT_EXPORT_MODULE()

+ (BOOL)requiresMainQueueSetup {
    return true;
}

- (dispatch_queue_t)methodQueue {
    return dispatch_get_main_queue();
}

RCT_EXPORT_METHOD(picturePick:(NSDictionary *)config resolver:(RCTPromiseResolveBlock)resolver rejecter:(RCTPromiseRejectBlock)rejecter) {
    YdkAlbumPickerConfig *apConfig = [self tranformAblumPickerConfig:config];
    [[YdkAlbum presentImagePickerWithConfig:apConfig sourceViewController:[UIViewController currentViewController]] subscribeNext:^(NSArray *x) {
        
        NSMutableDictionary *result = [NSMutableDictionary dictionary];
        
        if ([x.firstObject isKindOfClass:[YdkVideoInfo class]]) {
            __block NSMutableArray *videos = [NSMutableArray arrayWithCapacity:x.count];
            [x enumerateObjectsUsingBlock:^(YdkVideoInfo *obj, NSUInteger idx, BOOL *stop) {
                NSDictionary *JSONObj = @{@"filePath" : obj.filePath,
                                          @"fileName" : obj.fileName,
                                          @"thumbnailPath" : obj.thumbnailPath,
                                          @"duration" : [NSNumber numberWithInt:obj.duration],
                                          @"size" : [NSNumber numberWithLongLong:obj.size]
                                          };
                [videos addObject:JSONObj];
            }];
            result[@"videos"] = [NSArray arrayWithArray:videos];
            result[@"type"] = @(YdkAlbumPickerTypeVideo);
        } else {
            result[@"images"] = [NSArray arrayWithArray:x];
            result[@"type"] = @(YdkAlbumPickerTypeAll);
        }
        resolver(result);
    } error:^(NSError * _Nullable error) {
        rejecter(@(error.code).stringValue, [error.userInfo objectForKey:NSLocalizedDescriptionKey], error);
    }];
}

- (YdkAlbumPickerConfig *)tranformAblumPickerConfig:(NSDictionary *)config {
    YdkAlbumPickerConfig *apConfig = [YdkAlbumPickerConfig new];
    
    apConfig.type = [[config objectForKey:@"type"] integerValue];
    
    NSDictionary *style = [config objectForKey:@"style"];
    apConfig.numColumns = [[style objectForKey:@"numColumns"] integerValue];
    apConfig.showCamera = [[style objectForKey:@"showCamera"] boolValue];
    
    NSDictionary *picture = [config objectForKey:@"picture"];
    apConfig.maxNum = [[picture objectForKey:@"maxNum"] integerValue];
    apConfig.crop = [[picture objectForKey:@"isCrop"] boolValue];
    apConfig.cropScale = [[picture objectForKey:@"cropScale"] floatValue];
    
    return apConfig;
}

@end
