package ydk.audio.react;

import android.Manifest;
import android.app.Activity;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import ydk.audio.RecorderManager;
import ydk.core.Ydk;

/**
 * Created by Kyz on 2018/10/26.
 */
public class AudioRecorderModule extends ReactContextBaseJavaModule implements LifecycleEventListener {
    private static final String RESULT = "1";
    private static final String ERROR = "2";
    private static final String PROGRESS = "3";
    private final ReactApplicationContext mContext;

    public AudioRecorderModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
        reactContext.addLifecycleEventListener(this);
    }
    @Override
    public String getName() {
        return "YdkAudioRecorderModule";
    }

    //录音
    @ReactMethod
    public void startRecord(ReadableMap config, Promise promise) {
        Activity currentActivity = getCurrentActivity();
        if(currentActivity ==null){
            promise.reject("500", "currentActivity is null");
            return;
        }
        Ydk.getPermissions(currentActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .subscribe(grad -> {
                    if (grad) {
                        RecorderManager.getInstance().setRecorderListener(recorderListener);

                        int minDuration = config.getInt("minDuration");

                        int maxDuration = config.getInt("maxDuration");

                        RecorderManager.getInstance().startRecord(minDuration, maxDuration);

                        promise.resolve(null);
                    } else {
                        promise.reject("500", "获取权限失败");
                    }
                });
    }

    @ReactMethod
    public void stopRecord() {
        RecorderManager.getInstance().stopRecord();
    }

    //测试
    @ReactMethod
    public void startRecordPlay() {
        RecorderManager.getInstance().startRecordPlay();
    }

    private void sendEvent(String eventName, ReadableMap map) {
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, map);
    }

    RecorderManager.RecorderListener recorderListener = new RecorderManager.RecorderListener() {
        @Override
        public void getSatate(String flag, int duration, String filePath, long size, int progress, int db, int maxDuration) {
            switch (flag) {
                case RESULT:
                    WritableMap params = Arguments.createMap();
                    params.putInt("duration", duration);
                    params.putString("filePath", filePath);
                    params.putDouble("size", size);
                    sendEvent("OnRecordResult", params);
                    break;
                case ERROR:
                    sendEvent("OnRecordError", null);
                    break;
                case PROGRESS:
                    WritableMap paramss = Arguments.createMap();
                    paramss.putInt("progress", progress);
                    paramss.putInt("db", db);
                    paramss.putInt("maxDuration", maxDuration);
                    sendEvent("OnRecordProgress", paramss);
                    break;
            }

        }
    };

    @Override
    public void onHostResume() {

    }

    @Override
    public void onHostPause() {

    }

    @Override
    public void onHostDestroy() {
        RecorderManager.getInstance().releaseRecorder();
    }
}
