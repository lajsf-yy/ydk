package com.yryz.network.io.service;

import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSPlainTextAKSKCredentialProvider;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.yryz.network.NetworkConfig;
import com.yryz.network.io.entity.UploadInfo;


import java.io.File;
import java.util.Calendar;
import java.util.Collections;
import java.util.UUID;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ydk.core.Ydk;
import ydk.core.YdkConfigManager;
import ydk.core.utils.FileUtils;
import ydk.core.utils.image.ImageCompress;
import ydk.core.utils.image.ImageInfo;

public class OssUploadService implements UploadService {
    public static final String endpoint = "oss-cn-hangzhou.aliyuncs.com";
    public static String Tag = "OssService";
    public String bucketName = "";
    private OSS ossClient = null;
    private NetworkConfig networkConfig = YdkConfigManager.getConfig(NetworkConfig.class);


    public OssUploadService() {
        OSSCredentialProvider credentialProvider = new OSSPlainTextAKSKCredentialProvider(networkConfig.getAccessKeyId(), networkConfig.getSecretAccessKey());
        ClientConfiguration conf = new ClientConfiguration();
        conf.setConnectionTimeout(20 * 1000); // 连接超时，默认15秒
        conf.setSocketTimeout(20 * 1000); // socket超时，默认15秒
        conf.setMaxConcurrentRequest(5); // 最大并发请求书，默认5个
        conf.setMaxErrorRetry(2); // 失败后最大重试次数，默认2次
        ossClient = new OSSClient(Ydk.getApplicationContext(), endpoint, credentialProvider, conf);
        this.bucketName = networkConfig.getBucketName();
    }

    @Override
    public Observable<UploadInfo> upload(String localFile) {
        return upload(localFile, false);
    }

    public Observable<UploadInfo> upload(String localFile, boolean isHead) {
        if (FileUtils.isImage(localFile)) {
            return ImageCompress.with(Ydk.getApplicationContext()).setMinSize(300)
                    .load(Collections.singletonList(localFile)).compress()
                    .map(result -> {
                        ImageInfo imageInfo = result.get(0);
                        File file = imageInfo.getFile();
                        OssUploadInfo uploadInfo = createUploadInfo(bucketName, file.getAbsolutePath(), isHead);
                        uploadInfo.setWidth(imageInfo.getWidth());
                        uploadInfo.setHeight(imageInfo.getHeight());
                        return uploadInfo;
                    }).flatMap(uploadInfo -> Observable.create(new UploadSubscribe(uploadInfo)).subscribeOn(Schedulers.io()))
                    .observeOn(AndroidSchedulers.mainThread())  //在主线程回调
                    .subscribeOn(Schedulers.io());  //在子线程执行;
        } else {
            OssUploadInfo uploadInfo = createUploadInfo(bucketName, localFile);
            return Observable.create(new UploadSubscribe(uploadInfo))
                    .observeOn(AndroidSchedulers.mainThread())  //在主线程回调
                    .subscribeOn(Schedulers.io());  //在子线程执行
        }
    }

    @Override
    public Observable<UploadInfo> uploadHead(String localFile) {
        return upload(localFile, true);
    }

    private OssUploadInfo createUploadInfo(String bucketName, String localFile) {
        return createUploadInfo(bucketName, localFile, false);
    }

    private OssUploadInfo createUploadInfo(String bucketName, String localFile, boolean isHead) {
        String uuid = UUID.randomUUID().toString();
        String objectKey = getOssFilePath(localFile, isHead) + uuid;
        objectKey += getExtension(localFile);
        OssUploadInfo uploadInfo = new OssUploadInfo();
        uploadInfo.setBucketName(bucketName);
        uploadInfo.setObjectKey(objectKey);
        uploadInfo.setLocalFile(localFile);
        return uploadInfo;
    }

    private String getOssFilePath(String localFile, boolean isHead) {
        Calendar calendar = Calendar.getInstance();  //获取当前时间，作为图标的名字
        String year = calendar.get(Calendar.YEAR) + "";
        String month = calendar.get(Calendar.MONTH) + 1 + "";

        if (isHead) {
            return networkConfig.getName() + "/" + "head" + "/" + "android" + "/" + year + month + "/";
        } else {
            if (FileUtils.isAudio(localFile)) {
                return networkConfig.getName() + "/" + "audio" + "/" + "android" + "/" + year + month + "/";
            } else if (FileUtils.isVideo(localFile)) {
                return networkConfig.getName() + "/" + "video" + "/" + "android" + "/" + year + month + "/";
            } else if (FileUtils.isImage(localFile)) {
                return networkConfig.getName() + "/" + "image" + "/" + "android" + "/" + year + month + "/";
            }
            return networkConfig.getName() + "/" + "html" + "/" + "android" + "/" + year + month + "/";
        }
    }

    private String getExtension(String url) {
        return FileUtils.getSuffix(url);
    }

    private String getMimeType(String url) {
        String type = null;
        String extension = getExtension(url);
        if (!TextUtils.isEmpty(extension)) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        if (TextUtils.isEmpty(type))
            return "common";
        return type.split("/")[0];

    }

    class UploadSubscribe implements ObservableOnSubscribe<UploadInfo> {
        private OssUploadInfo uploadInfo;

        public UploadSubscribe(OssUploadInfo uploadInfo) {
            this.uploadInfo = uploadInfo;
        }

        @Override
        public void subscribe(ObservableEmitter<UploadInfo> emitter) throws Exception {
            PutObjectRequest put = new PutObjectRequest(uploadInfo.getBucketName(), uploadInfo.getObjectKey(), uploadInfo.getLocalFile());
            put.setProgressCallback((putObjectRequest, uploadBytes, total) -> {
                UploadInfo uploadInfo = new UploadInfo();
                uploadInfo.setTotal(total);
                uploadInfo.setUploadBytes(uploadBytes);
                if (uploadBytes != total) {
                    emitter.onNext(uploadInfo);
                }
            });
            PutObjectResult result = ossClient.putObject(put);
            if (!TextUtils.isEmpty(result.getRequestId()) && !TextUtils.isEmpty(result.getETag())) {
                String url = networkConfig.getCdn() + uploadInfo.getObjectKey();
                uploadInfo.setUrl(url);
            } else {
                Log.e(Tag, result.getServerCallbackReturnBody());
            }
            uploadInfo.setCompleted(true);
            emitter.onNext(uploadInfo);
            emitter.onComplete();
        }
    }

    class OssUploadInfo extends UploadInfo {
        String bucketName;
        String objectKey;

        public String getBucketName() {
            return bucketName;
        }

        public void setBucketName(String bucketName) {
            this.bucketName = bucketName;
        }

        public String getObjectKey() {
            return objectKey;
        }

        public void setObjectKey(String objectKey) {
            this.objectKey = objectKey;
        }
    }
}
