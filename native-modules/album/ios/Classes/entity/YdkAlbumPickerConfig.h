//
//  YdkAlbumPickerConfig.h
//  ydk-album
//
//  Created by yryz on 2019/7/4.
//

#import <Foundation/Foundation.h>

typedef NS_ENUM(NSInteger, YdkAlbumPickerType) {
    YdkAlbumPickerTypeImage = 1,
    YdkAlbumPickerTypeVideo = 2,
    YdkAlbumPickerTypeAll = 0
};

@interface YdkAlbumPickerConfig : NSObject

// 1. type
@property (nonatomic, assign) YdkAlbumPickerType type;

// 2. style
@property (nonatomic, assign) NSInteger numColumns;
@property (nonatomic, assign, getter=isShowCamera) BOOL showCamera;

// 3. config
@property (nonatomic, assign) NSInteger maxNum;
@property (nonatomic, assign, getter=isCrop) BOOL crop;
@property (nonatomic, assign) float cropScale;

@end
