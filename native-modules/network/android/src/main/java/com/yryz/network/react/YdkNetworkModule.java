package com.yryz.network.react;


import android.app.Activity;

import com.facebook.react.bridge.Dynamic;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;
import com.yryz.network.NetworkConfig;
import com.yryz.network.YdkNetwork;
import com.yryz.network.io.entity.DownloadInfo;
import com.yryz.network.io.entity.UploadInfo;

import org.json.JSONArray;
import org.json.JSONObject;

import io.reactivex.Observable;
import ydk.core.YdkConfigManager;
import ydk.react.ReactUtils;

public class YdkNetworkModule extends ReactContextBaseJavaModule {
    private ReactApplicationContext reactContext;

    private YdkNetwork ydkNetwork;

    public YdkNetworkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        this.ydkNetwork = new YdkNetwork(reactContext, YdkConfigManager.getConfig(NetworkConfig.class));
    }

    @Override
    public String getName() {
        return "YdkNetworkModule";
    }

    /**
     *
     * @param filePath
     * @param fileType fileType: head（头像），image（图片），audio（音频），video（视频）
     * @param promise
     */
    @ReactMethod
    public void upload(String filePath, String fileType, Promise promise) {
        if ("head".equals(fileType)) {
            uploadHeadImg(filePath, promise);
        } else {
            upload(filePath, promise);
        }
    }
    public void upload(String filePath, Promise promise) {
        Observable<UploadInfo> observable = ydkNetwork.upload(filePath);
        ReactUtils.subscribe(observable, promise);
    }

    @ReactMethod
    public void uploadHeadImg(String filePath, Promise promise) {
        Observable<UploadInfo> observable = ydkNetwork.uploadHeadImg(filePath);
        ReactUtils.subscribe(observable, promise);
    }

    @ReactMethod
    public void download(String url, Promise promise) {
        Observable<DownloadInfo> observable = ydkNetwork.download(getCurrentActivity(), url);
        ReactUtils.subscribe(observable, promise);
    }

    @ReactMethod
    public void downloadImage(String url, Promise promise) {
        Observable<DownloadInfo> observable = ydkNetwork.downloadImage(getCurrentActivity(), url);
        ReactUtils.subscribe(observable, promise);
    }
    private Observable<Object> bindToLifecycle(Observable<Object> observable) {
//        Activity currentActivity = getCurrentActivity();
//        if (currentActivity != null && currentActivity instanceof RxAppCompatActivity) {
//            RxAppCompatActivity rxAppCompatActivity = (RxAppCompatActivity) currentActivity;
//            return observable.compose(rxAppCompatActivity.bindToLifecycle());
//        }
        return observable;
    }

    @ReactMethod
    public void get(String url, ReadableMap readableMap, Promise promise) {

        Observable<Object> observable = ydkNetwork.get(url, readableMap != null ? readableMap.toHashMap() : null);

        ReactUtils.subscribe(bindToLifecycle(observable), promise);
    }


    @ReactMethod
    public void post(String url, Dynamic dynamic, Promise promise) {

        Observable<Object> observable = ydkNetwork.post(url, ReactUtils.dynamicToJSON(dynamic));

        ReactUtils.subscribe(bindToLifecycle(observable), promise);
    }


    @ReactMethod
    public void put(String url, Dynamic dynamic, Promise promise) {

        Observable<Object> observable = ydkNetwork.put(url, ReactUtils.dynamicToJSON(dynamic));

        ReactUtils.subscribe(bindToLifecycle(observable), promise);
    }


    @ReactMethod
    public void delete(String url, Dynamic dynamic, Promise promise) {

        Observable<Object> observable = ydkNetwork.delete(url, ReactUtils.dynamicToJSON(dynamic));

        ReactUtils.subscribe(bindToLifecycle(observable), promise);
    }
}
