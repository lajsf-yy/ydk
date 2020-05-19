//
//  YdkAlbum.m
//  ydk-album
//
//  Created by yryz on 2019/7/4.
//

#import "YdkAlbum.h"
#import "YdkVideoInfo.h"

#import <ydk-core/YdkCore.h>
#import <ydk-toolkit/YdkToolkit.h>
#import <TZImagePickerController/TZImagePickerController.h>
#import <SVProgressHUD/SVProgressHUD.h>

NSErrorDomain const YdkAlbumErrorDomain = @"YdkAlbumErrorDomain";

@interface YdkAlbum () <TZImagePickerControllerDelegate>

@end

@implementation YdkAlbum
{
    RACSubject *_subject;
}

+ (void)load {
    ydk_register_module(self);
}

+ (RACSignal<NSArray/*[FilePath] or YdkVideoInfo*/ *> *)presentImagePickerWithConfig:(YdkAlbumPickerConfig *)config sourceViewController:(UIViewController *)sourceVC {
    YdkAlbum *module = ydk_get_module_instance(self);
    return [module presentImagePickerWithConfig:config sourceViewController:sourceVC];
}

- (RACSignal<NSArray/*[FilePath] or YdkVideoInfo*/ *> *)presentImagePickerWithConfig:(YdkAlbumPickerConfig *)config sourceViewController:(UIViewController *)sourceVC {
    if (!sourceVC) {
        return [RACSignal createSignal:^RACDisposable * _Nullable(id<RACSubscriber>  _Nonnull subscriber) {
            NSError *error = [NSError errorWithDomain:YdkAlbumErrorDomain code:YdkAlbumErrorDomainNotFoundCurrentViewController userInfo:@{NSLocalizedDescriptionKey : @"sourceViewController is nil."}];
            [subscriber sendError:error];
            return nil;
        }];
    }
    if (config.isCrop) {
        config.maxNum = 1;
    }
    TZImagePickerController *imagePicker = [[TZImagePickerController alloc] initWithMaxImagesCount:config.maxNum columnNumber:config.numColumns delegate:self];
    imagePicker.modalPresentationStyle = UIModalPresentationFullScreen;
    switch (config.type) {
            case YdkAlbumPickerTypeImage:
            imagePicker.allowPickingGif = YES;
            imagePicker.allowPickingVideo = NO;
            imagePicker.allowTakePicture = config.isShowCamera;
            break;
            case YdkAlbumPickerTypeVideo:
            imagePicker.allowPickingImage  = NO;
            imagePicker.allowPickingVideo = YES;
            imagePicker.allowTakeVideo = config.isShowCamera;
            break;
            case YdkAlbumPickerTypeAll:
            imagePicker.allowPickingGif = YES;
            imagePicker.allowTakePicture = config.isShowCamera;
            imagePicker.allowTakeVideo = config.isShowCamera;
            break;
    }
    imagePicker.allowCrop = config.isCrop;
    CGSize size = CGSizeMake(imagePicker.view.frame.size.width, imagePicker.view.frame.size.width);
    if (config.cropScale > 0) {
        size.height = MIN(imagePicker.view.frame.size.height, imagePicker.view.frame.size.width / config.cropScale);
    }
    CGFloat x = (imagePicker.view.frame.size.width - size.width) / 2.0;
    CGFloat y = (imagePicker.view.frame.size.height - size.height) / 2.0;
    imagePicker.cropRect = CGRectMake(x, y, size.width, size.height);
    [sourceVC presentViewController:imagePicker animated:YES completion:nil];
    _subject = [RACSubject subject];
    return _subject;
}

// MARK: - TZImagePickerControllerDelegate
- (void)imagePickerController:(TZImagePickerController *)picker didFinishPickingPhotos:(NSArray *)photos sourceAssets:(NSArray *)assets isSelectOriginalPhoto:(BOOL)isSelectOriginalPhoto {
    __block NSInteger total = assets.count;
    NSMutableArray *imagePaths= [NSMutableArray arrayWithCapacity:assets.count];
    if (!isSelectOriginalPhoto) {
        for (UIImage *image in photos) {
            NSString *imageUrl = [self saveImage:UIImageJPEGRepresentation(image, 1.0) fileName:[NSString tk_randomUUID]];
            if (imageUrl) {
                [imagePaths addObject:imageUrl];
            } else {
                total--;
            }
            if (total == imagePaths.count) {
                [self->_subject sendNext:imagePaths];
                [self->_subject sendCompleted];
            }
        }
    } else {
        for (PHAsset *asset in assets) {
            __weak typeof(self) weakSelf = self;
            [[TZImageManager manager] getOriginalPhotoDataWithAsset:asset completion:^(NSData *data, NSDictionary *info, BOOL isDegraded) {
                __strong typeof(weakSelf) strongSelf = weakSelf;
                NSString *imageUrl = [strongSelf saveImage:data fileName:asset.localIdentifier];
                if (imageUrl) {
                    [imagePaths addObject:imageUrl];
                } else {
                    total--;
                }
                if (total == imagePaths.count) {
                    [strongSelf->_subject sendNext:imagePaths];
                    [strongSelf->_subject sendCompleted];
                }
            }];
        }
    }
}

- (NSString *)saveImage:(NSData *)data fileName:(NSString *)fileName {
    NSString *filePath = [NSString stringWithFormat:@"%@/%@.%@", [YdkFileSystemTool tempDirectoryForComponent:NSStringFromClass(self.class)], fileName, [data tk_imageFileSuffix]];
    if (![[NSFileManager defaultManager] fileExistsAtPath:filePath]) {
        filePath = [YdkFileSystemTool createTempFileURLWithComponent:NSStringFromClass(self.class) fileName:[NSString stringWithFormat:@"%@.%@", fileName, [data tk_imageFileSuffix]]].path;
        // 保存到临时文件并返回filePath
        BOOL result = [YdkImageTool saveImage:data targetURL:[NSURL fileURLWithPath:filePath]];
        if (!result) {
            return nil;
        }
    }
    return filePath;
}

- (void)imagePickerController:(TZImagePickerController *)picker didSelectVideoOfPath:(NSString *)filePath thumbImage:(UIImage *)image {
    if (filePath && image) {
        YdkVideoInfo *videoInfo = [YdkVideoInfo videoInfoWithVideoURL:[NSURL fileURLWithPath:filePath]];
        [self->_subject sendNext:[NSArray arrayWithObjects:videoInfo, nil]];
        [self->_subject sendCompleted];
    }
}

- (void)exportFaildWithMessage:(NSString *)message {
    [SVProgressHUD showErrorWithStatus:message];
}

- (void)startWithExportMedia {
    [SVProgressHUD show];
    [SVProgressHUD showProgress:.0f];
}

- (void)finishedWithExportMediaOfStatus:(NSInteger)status progress:(float)progress {
    if (status == 2) {
        [SVProgressHUD showProgress:progress];
    } else {
        [SVProgressHUD dismissWithDelay:0.5];
    }
}

// MARK: - TZImagePickerControllerDelegate
- (void)imagePickerController:(TZImagePickerController *)picker didFinishPickingVideo:(UIImage *)coverImage sourceAssets:(id)asset {
    NSMutableArray<PHAsset *> *assets = [NSMutableArray array];
    NSMutableArray<YdkVideoInfo *> *videoInfos = [NSMutableArray array];
    if ([asset isKindOfClass:[PHAsset class]]) {
        [assets addObject:asset];
    } else if ([asset isKindOfClass:[NSArray class]]) {
        [assets addObjectsFromArray:asset];
    }
    
    __block NSInteger total = assets.count;
    for (PHAsset *phAsset in assets) {
        PHVideoRequestOptions *options = [[PHVideoRequestOptions alloc] init];
        options.version = PHImageRequestOptionsVersionCurrent;
        options.deliveryMode = PHVideoRequestOptionsDeliveryModeAutomatic;
        
        PHImageManager *manager = [PHImageManager defaultManager];
        // 获取视频信息
        [manager requestAVAssetForVideo:phAsset options:options resultHandler:^(AVAsset *asset, AVAudioMix *audioMix, NSDictionary *info) {
            AVURLAsset *urlAsset = (AVURLAsset *)asset;
            YdkVideoInfo *videoInfo = [YdkVideoInfo videoInfoWithVideoURL:urlAsset.URL];
            if (videoInfo) {
                [videoInfos addObject:videoInfo];
            } else {
                total -= 1;
            }
            if (total == videoInfos.count) {
                [self->_subject sendNext:videoInfos];
                [self->_subject sendCompleted];
            }
        }];
    }
}

- (void)tz_imagePickerControllerDidCancel:(TZImagePickerController *)picker {
    [picker dismissViewControllerAnimated:YES completion:nil];
    [_subject sendError:[NSError errorWithDomain:YdkAlbumErrorDomain code:YdkCancel.integerValue userInfo:@{NSLocalizedDescriptionKey : @"用户取消"}]];
    [_subject sendCompleted];
}
@end
