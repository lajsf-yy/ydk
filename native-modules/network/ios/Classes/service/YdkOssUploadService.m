//
//  YdkOssUploadService.m
//  ydk
//
//  Created by yryz on 2019/6/19.
//

#define END_POINT   @"http://oss-cn-hangzhou.aliyuncs.com"

#import "YdkOssUploadService.h"
#import "YdkUploadInfo.h"

#import <AliyunOSSiOS/OSSService.h>
#import <MobileCoreServices/MobileCoreServices.h>
#import <AVFoundation/AVFoundation.h>

#import <ydk-toolkit/YdkToolkit.h>

@interface YdkOssUploadService ()

@property (nonatomic, copy) NSString *accessKeyId;
@property (nonatomic, copy) NSString *secretAccessKey;
@property (nonatomic, copy) NSString *bucketName;
@property (nonatomic, copy) NSString *ossHost;

@property (nonatomic, copy) NSString *name;

@property (nonatomic, strong) NSMutableArray<OSSPutObjectRequest *> *requestArray;
@property (nonatomic, strong) dispatch_queue_t operationQueue;

@end

@implementation YdkOssUploadService

- (instancetype)init {
    if (self = [super init]) {
        _requestArray = [NSMutableArray array];
        _operationQueue = dispatch_queue_create("com.yryz.ydk.upload", DISPATCH_QUEUE_SERIAL);
    }
    return self;
}

- (instancetype)initWithAccessKeyId:(NSString *)accessKeyId secretAccessKey:(NSString *)secretAccessKey bucketName:(NSString *)bucketName directoryName:(NSString *)name ossHost:(NSString *)ossHost {
    _accessKeyId = [accessKeyId copy];
    _secretAccessKey = [secretAccessKey copy];
    _bucketName = [bucketName copy];
    _name = [name copy];
    _ossHost = [ossHost copy];
    return [self init];
}

- (OSSClient *)setupOSSClient {
    @weakify(self);
    id<OSSCredentialProvider> credential1 = [[OSSCustomSignerCredentialProvider alloc] initWithImplementedSigner:^NSString *(NSString *contentToSign, NSError *__autoreleasing *error) {
        @strongify(self);
        NSString *signature = [OSSUtil calBase64Sha1WithData:contentToSign withSecret:self.secretAccessKey];
        return [NSString stringWithFormat:@"OSS %@:%@", self.accessKeyId, signature];
    }];
    
    OSSClientConfiguration * conf = [OSSClientConfiguration new];
    conf.maxRetryCount = 5;
    conf.timeoutIntervalForRequest = 30;
    conf.timeoutIntervalForResource = 24 * 60 * 60;
    OSSClient *ossClient = [[OSSClient alloc] initWithEndpoint:END_POINT credentialProvider:credential1 clientConfiguration:conf];
    NSAssert(ossClient != nil, @"OSSClient实例化失败");
    return ossClient;
}

// MARK: - HttpUploadServiceProtocol
- (RACSignal<YdkUploadInfo *> *)uploadWithData:(NSData *)uploadData fileType:(NSString *)fileType {
    return [self uploadWithData:uploadData fileURL:nil fileType:fileType];
}

- (RACSignal<YdkUploadInfo *> *)uploadWithFileURL:(NSURL *)fileURL fileType:(NSString *)fileType {
    return [self uploadWithData:nil fileURL:fileURL fileType:fileType];
}

- (RACSignal<YdkUploadInfo *> *)uploadWithData:(NSData *)uploadData fileURL:(NSURL *)fileURL fileType:(NSString *)fileType {
    if (!_accessKeyId || !_secretAccessKey || !_bucketName || !_name || !_ossHost) {
        RACReplaySubject *subject = [RACReplaySubject subject];
        NSError *error = [NSError errorWithDomain:YdkNetworkErrorDomain code:YdkNetworkErrorLackOSSParams userInfo:@{@"message" : @"缺少OSS上传参数."}];
        [subject sendError:error];
        return subject;
    }
    
    
    RACSubject *subject = [RACSubject subject];
    __block YdkUploadInfo *uploadInfo = [[YdkUploadInfo alloc] init];
    
    OSSPutObjectRequest *put = [OSSPutObjectRequest new];
    dispatch_async(_operationQueue, ^{
        [self.requestArray addObject:put];
    });
    NSString *objectKey;
    if (uploadData) {
        if (!fileType) {
            fileType = @"image";
        }
        objectKey = [NSString stringWithFormat:@"%@/%@/ios/%@/%@.%@", _name, fileType, [self _currentYearAndMonth], [NSString tk_randomUUID], [uploadData tk_imageFileSuffix]];
        put.uploadingData = uploadData;
        uploadInfo.ext = @{@"size": NSStringFromCGSize([UIImage imageWithData:uploadData].size)};
    } else if (fileURL) {
        if (!fileType) {
            fileType = [self _directoryWithFileURL:fileURL];
        }
        objectKey = [NSString stringWithFormat:@"%@/%@/ios/%@/%@.%@", _name, fileType, [self _currentYearAndMonth], [NSString tk_randomUUID], [self _mediaFileSuffixWithFileURL:fileURL]];
        put.uploadingFileURL = fileURL;
        if (fileURL.tk_isVideoFile) {
            AVAsset *asset = [AVAsset assetWithURL:fileURL];
            AVAssetTrack *track = [asset tracksWithMediaType:AVMediaTypeVideo].firstObject;
            uploadInfo.ext = @{@"size": NSStringFromCGSize(track.naturalSize)};
        }
    }
    
    put.bucketName = _bucketName;
    put.objectKey = objectKey;
    put.uploadProgress = ^(int64_t bytesSent, int64_t totalBytesSent, int64_t totalBytesExpectedToSend) {
        //        uploadInfo.bytesSent = bytesSent;
        uploadInfo.totalBytesSent = totalBytesSent;
        uploadInfo.totalBytesExpectedToSend = totalBytesExpectedToSend;
        [subject sendNext:uploadInfo];
    };
    OSSTask *putTask = [[self setupOSSClient] putObject:put];
    NSString *ossHost = [_ossHost copy];
    @weakify(self);
    [putTask continueWithBlock:^id(OSSTask *task) {
        @strongify(self);
        if(!task.error) {
            NSString *url = [ossHost stringByAppendingString:objectKey];
            uploadInfo.url = url;
            uploadInfo.completed = YES;
            [subject sendNext:uploadInfo];
            [subject sendCompleted];
        } else {
            [subject sendError:task.error];
            [subject sendCompleted];
        }
        dispatch_async(self.operationQueue, ^{
            [self.requestArray removeObject:put];
        });
        return nil;
    }];
    return subject;
}

// MARK: - Utils

// 通过文件URL获取媒体类型 .mp4 .mp3 .gif .jpg
- (NSString *)_mediaFileSuffixWithFileURL:(NSURL *)fileURL {
    if (fileURL.isFileURL || fileURL.isFileReferenceURL) {
        if (fileURL.tk_isVideoFile) {
            return @"mp4";
        } else if (fileURL.tk_isImageFile) {
            NSString *UTI = fileURL.tk_UTI;
            if ([UTI rangeOfString:@"gif"].location != NSNotFound) {
                return @"gif";
            } else {
                return @"jpg";
            }
        } else if (fileURL.tk_isAudioFile) {
            return @"mp3";
        }
    }
    return @"";
}

// 通过文件URL获取OSS目录存放位置
- (NSString *)_directoryWithFileURL:(NSURL *)fileURL {
    if (fileURL.isFileURL || fileURL.isFileReferenceURL) {
        if (fileURL.tk_isVideoFile) {
            return @"video";
        } else if (fileURL.tk_isImageFile) {
            return @"image";
        } else if (fileURL.tk_isAudioFile) {
            return @"audio";
        }
    }
    return @"common";
}

// 获取当前年月份
- (NSString *)_currentYearAndMonth {
    NSDateComponents *dc = [[NSCalendar currentCalendar] components:NSCalendarUnitYear fromDate:[NSDate date]];
    return [NSString stringWithFormat:@"%ld%02ld", (long)dc.year, (long)dc.month];
}

@end
