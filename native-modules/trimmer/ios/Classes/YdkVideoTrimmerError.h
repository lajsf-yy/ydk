//
//  YdkVideoTrimmerErrorDomain.h
//  ydk-trimmer
//
//  Created by yryz on 2019/9/4.
//

FOUNDATION_EXPORT NSErrorDomain const YdkVideoTrimmerErrorDomain;

NS_ERROR_ENUM(YdkVideoTrimmerErrorDomain)
{
    YdkVideoTrimmerErrorInvalidAVAsset                    = -1000,
    YdkVideoTrimmerErrorInvalidAVAssetTrack               = -1001,
    YdkVideoTrimmerErrorInvalidAVMutableCompositionTrack  = -1002,
    YdkVideoTrimmerErrorInvalidAVAssetExportSession       = -1003,
    YdkVideoTrimmerErrorSaveVideoThumbnailFailed          = -1004,
};
