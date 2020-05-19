package com.yryz.network;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import com.yryz.network.http.HttpClient;
import com.yryz.network.http.model.BaseModel;
import com.yryz.network.http.network.NeetWork;
import com.yryz.network.http.token.DeviceUUID;
import com.yryz.network.http.token.TokenIllegalStateException;
import com.yryz.network.http.transform.MyObject;
import com.yryz.network.http.transform.Transform;
import com.yryz.network.io.entity.DownloadInfo;
import com.yryz.network.io.entity.UploadInfo;
import com.yryz.network.io.service.DownloadService;
import com.yryz.network.io.service.OssUploadService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import ydk.annotations.YdkModule;
import ydk.core.Ydk;
import ydk.core.utils.ImageUtils;
import ydk.core.utils.JsonUtils;
import ydk.react.error.ResultException;

//@YdkModule(export = true)
public class YdkNetwork {
    private NetworkConfig mNetworkConfig;
    private Context mContext;
    private OssUploadService uploadService = null;
    private DownloadService downloadService = null;

    public YdkNetwork(Context context, NetworkConfig networkConfig) {
        mNetworkConfig = networkConfig;
        mContext = context;
        NeetWork.init(context);
        uploadService = new OssUploadService();
        downloadService = new DownloadService();
    }

    public NetworkConfig getNetworkConfig() {
        return mNetworkConfig;
    }

    public Observable<UploadInfo> upload(String filePath) {

        return uploadService.upload(filePath)
                .filter(uploadInfo -> {
                    if (uploadInfo.isCompleted()) {
                        return true;
                    }
                    sendEvent("uploadProcess", uploadInfo);
                    return false;
                });
    }


    public Observable<UploadInfo> uploadHeadImg(String filePath) {

        return uploadService.uploadHead(filePath)
                .filter(uploadInfo -> {
                    if (uploadInfo.isCompleted()) {
                        return true;
                    }
                    sendEvent("uploadProcess", uploadInfo);
                    return false;
                });

    }


    public Observable<DownloadInfo> download(Activity activity, String url) {

        return Ydk.getPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .flatMap((Function<Boolean, ObservableSource<DownloadInfo>>) grad -> {
                    if (grad) {
                        return downloadService.download(url);
                    }
                    return Observable.error(new IllegalArgumentException("没有文件读写权限"));
                }).filter(downloadInfo -> {
                    if (downloadInfo.isCompleted()) {
                        updatePhotoAlbum(downloadInfo.getFilePath());
                        return true;
                    }
                    sendEvent("downloadProcess", downloadInfo);
                    return false;
                });
    }


    public Observable<DownloadInfo> downloadImage(Activity activity, String url) {

        return Ydk.getPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .flatMap((Function<Boolean, ObservableSource<DownloadInfo>>) grad -> {
                    if (grad) {
                        return downloadService.downloadByFresco(url);
                    }
                    return Observable.error(new IllegalArgumentException("没有文件读写权限"));
                }).filter(downloadInfo -> {
                    if (downloadInfo.isCompleted()) {
                        updatePhotoAlbum(downloadInfo.getFilePath());
                        return true;
                    }
                    sendEvent("downloadProcess", downloadInfo);
                    return false;
                });

    }

    private void updatePhotoAlbum(String filePath) {

        Observable.create((ObservableOnSubscribe<String>) emitter -> {
            ImageUtils.updatePhotoAlbum(mContext.getApplicationContext(), filePath, true);
            emitter.onComplete();
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> {

                });
    }

    /**
     * get 请求
     *
     * @param url
     * @param readableMap
     * @return
     */
    public Observable<Object> get(String url, Map<String, Object> readableMap) {
        Map<String, String> params = new HashMap<>();

        if (readableMap != null) {
            params = JsonUtils.toStrMap(new JSONObject(readableMap).toString());
        }
        Observable<ResponseBody> observable = HttpClient.INSTANCE.getClient().get(url, params);

        return transResult(observable);

    }

    /**
     * post 请求
     *
     * @param url
     * @param josn
     * @return
     */
    public Observable<Object> post(String url, String josn) {

        Observable<ResponseBody> observable = null;
        if (TextUtils.isEmpty(josn)) {
            observable = HttpClient.INSTANCE.getClient().post(url, new HashMap<>());
        } else {
            observable = HttpClient.INSTANCE.getClient().post(url, josn);
        }
        return transResult(observable);
    }

    /**
     * post 请求
     *
     * @param url
     * @param readableMap
     * @return
     */
    public Observable<Object> post(String url, Map<String, Object> readableMap) {

        Observable<ResponseBody> observable = null;
        if (readableMap == null || readableMap.isEmpty()) {
            observable = HttpClient.INSTANCE.getClient().post(url, new HashMap<>());
        } else {
            observable = HttpClient.INSTANCE.getClient().post(url, new JSONObject(readableMap).toString());
        }
        return transResult(observable);
    }

    /**
     * post 请求
     *
     * @param url
     * @param readableList
     * @return
     */
    public Observable<Object> post(String url, List<Object> readableList) {

        Observable<ResponseBody> observable = null;
        if (readableList == null || readableList.isEmpty()) {
            observable = HttpClient.INSTANCE.getClient().post(url, new HashMap<>());
        } else {
            observable = HttpClient.INSTANCE.getClient().post(url, new JSONArray(readableList).toString());
        }
        return transResult(observable);
    }

    /**
     * @param url
     * @param json
     * @return
     */
    public Observable<Object> put(String url, String json) {

        Observable<ResponseBody> observable = null;
        if (TextUtils.isEmpty(json)) {
            observable = HttpClient.INSTANCE.getClient().put(url, new HashMap<>());
        } else {
            observable = HttpClient.INSTANCE.getClient().put(url, json);
        }
        return transResult(observable);
    }

    /**
     * post 请求
     *
     * @param url
     * @param readableMap
     * @return
     */
    public Observable<Object> put(String url, Map<String, Object> readableMap) {

        Observable<ResponseBody> observable = null;
        if (readableMap == null || readableMap.isEmpty()) {
            observable = HttpClient.INSTANCE.getClient().put(url, new HashMap<>());
        } else {
            observable = HttpClient.INSTANCE.getClient().put(url, new JSONObject(readableMap).toString());
        }
        return transResult(observable);
    }

    /**
     * post 请求
     *
     * @param url
     * @param readableList
     * @return
     */
    public Observable<Object> put(String url, List<Object> readableList) {

        Observable<ResponseBody> observable = null;
        if (readableList == null || readableList.isEmpty()) {
            observable = HttpClient.INSTANCE.getClient().put(url, new HashMap<>());
        } else {
            observable = HttpClient.INSTANCE.getClient().put(url, new JSONArray(readableList).toString());
        }
        return transResult(observable);
    }

    /**
     * put 请求
     *
     * @param url
     * @param json
     * @return
     */
    public Observable<Object> delete(String url, String json) {

        Observable<ResponseBody> observable = null;
        if (TextUtils.isEmpty(json)) {
            observable = HttpClient.INSTANCE.getClient().delete(url, "");
        } else {
            observable = HttpClient.INSTANCE.getClient().delete(url, json);
        }
        return transResult(observable);
    }

    /**
     * put 请求
     *
     * @param url
     * @param readableMap
     * @return
     */
    public Observable<Object> delete(String url, Map<String, Object> readableMap) {

        Observable<ResponseBody> observable = null;
        if (readableMap == null || readableMap.isEmpty()) {
            observable = HttpClient.INSTANCE.getClient().delete(url, "");
        } else {
            observable = HttpClient.INSTANCE.getClient().delete(url, new JSONObject(readableMap).toString());
        }
        return transResult(observable);
    }

    /**
     * put 请求
     *
     * @param url
     * @param readableList
     * @return
     */
    public Observable<Object> delete(String url, List<Object> readableList) {

        Observable<ResponseBody> observable = null;
        if (readableList == null || readableList.isEmpty()) {
            observable = HttpClient.INSTANCE.getClient().delete(url, "");
        } else {
            observable = HttpClient.INSTANCE.getClient().delete(url, new JSONArray(readableList).toString());
        }
        return transResult(observable);
    }

    /**
     * @param observable 结果转换
     *                   *
     * @return
     */
    private Observable<Object> transResult(Observable<ResponseBody> observable) {

        return observable.flatMap((Function<ResponseBody, ObservableSource<Object>>) responseBody -> {
            String responseBodyString = responseBody.string();
            if (TextUtils.isEmpty(responseBodyString)) {
                return Observable.error(new ResultException("500", "数据错误"));
            }
            BaseModel objectsBaseModel = Transform.Companion.fromJsonObject(responseBodyString, MyObject.class);
            if (objectsBaseModel == null) {
                return Observable.error(new ResultException("500", "数据错误"));
            }
            String code = objectsBaseModel.getCode();
            if ("200".equals(code)) {
                return Observable.just(objectsBaseModel);
            }
            return Observable.error(new ResultException(code, objectsBaseModel.getMsg(), objectsBaseModel));
        }).retryWhen(throwableObservable ->
                throwableObservable.flatMap(throwable -> {
                    if (throwable instanceof TokenIllegalStateException) {

                        TokenIllegalStateException tokenIllegalStateException = (TokenIllegalStateException) throwable;

                        return Observable.error(new ResultException(
                                tokenIllegalStateException.getCode(),
                                throwable.getMessage(),
                                tokenIllegalStateException.getObject()));
                    }
                    return Observable.error(throwable);
                })).subscribeOn(Schedulers.io());
    }


    private void sendEvent(String eventName, Object data) {
        Ydk.getEventEmitter().emit(eventName, data);

    }

}
