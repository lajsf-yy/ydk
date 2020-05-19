package com.yryz.netty.react;

import android.os.Handler;
import android.os.Looper;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.yryz.netty.client.Netty;
import com.yryz.netty.client.NettyReqWrapper;
import com.yryz.netty.cmd.cmd.CommondEnum;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;


public class SocketModule extends ReactContextBaseJavaModule {

    private static ReactApplicationContext reactApplicationContext;

    public final static String observeReceiveMessage = "observeReceiveMessage";

    private Disposable mDisposable;

    public SocketModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactApplicationContext = reactContext;
    }


    @Override
    public String getName() {
        return "SocketModule";
    }

    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> map = new HashMap<>();
        return map;
    }

    @ReactMethod
    public void sendMessage(String string, Promise promise) {

        Runnable runnable = () -> {
            Netty.sendMessage(readableMapToNettyReqWrapper(string)).subscribe(aBoolean -> {
                promise.resolve(aBoolean);
            }, throwable -> {
                promise.resolve(false);
            });
        };
        if (getCurrentActivity() != null) {
            getCurrentActivity().runOnUiThread(runnable);
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }

    }

    private NettyReqWrapper readableMapToNettyReqWrapper(String string) {

        NettyReqWrapper nettyReqWrapper = new NettyReqWrapper(CommondEnum.CMD_HEARTBEAT.getValue(), "");
        try {
            JSONObject jsonObject = new JSONObject(string);
            int cmd = jsonObject.optInt("cmd", 0);
            String data = "";
            if (jsonObject.has("data")) {
                JSONObject optJSONObject = jsonObject.optJSONObject("data");
                data = optJSONObject.toString();
            } else {
                data = new JSONObject().toString();
            }
            nettyReqWrapper = new NettyReqWrapper(cmd, data);

        } catch (JSONException e) {

        }
        return nettyReqWrapper;
    }

    private String nettyReqWrapperToWritableMap(NettyReqWrapper nettyReqWrapper) {

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("cmd", nettyReqWrapper.getCmd());
            JSONObject dataJsonObject = new JSONObject(nettyReqWrapper.getData());
            jsonObject.put("data", dataJsonObject);
        } catch (JSONException e) {

        }
        return jsonObject.toString();
    }


    private void registerObservable() {

        Netty.registerRNObservable()
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<NettyReqWrapper>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        mDisposable = d;
                    }

                    @Override
                    public void onNext(NettyReqWrapper nettyReqWrapper) {

                        emit(nettyReqWrapperToWritableMap(nettyReqWrapper));

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
        //
    }

    @ReactMethod
    public void start(Promise promise) {

        Runnable runnable = () -> {
            registerObservable();
            Netty.start();
            promise.resolve(true);
        };
        if (getCurrentActivity() != null) {
            getCurrentActivity().runOnUiThread(runnable);
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    @ReactMethod
    public void stop() {

        Runnable runnable = () -> {
            if (mDisposable != null && !mDisposable.isDisposed()) {
                mDisposable.dispose();
            }
            Netty.stop();
        };
        if (getCurrentActivity() != null) {
            getCurrentActivity().runOnUiThread(runnable);
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }


    private void emit(Object date) {
        emit(observeReceiveMessage, date);
    }

    private void emit(String eventName, Object date) {
        if (reactApplicationContext == null) {
            return;
        }
        try {
            reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
