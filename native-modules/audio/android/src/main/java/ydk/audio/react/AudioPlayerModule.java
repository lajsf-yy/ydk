package ydk.audio.react;

import android.Manifest;
import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import ydk.audio.AudioConfig;
import ydk.audio.AudioModel;
import ydk.audio.AudioModelEmitter;
import ydk.audio.Constant;
import ydk.audio.PlayerManager;
import ydk.core.Ydk;
import ydk.core.utils.MapUtils;


/**
 * Created by Kyz on 2018/10/26.
 */
public class AudioPlayerModule extends ReactContextBaseJavaModule implements LifecycleEventListener {

    private final ReactApplicationContext mContext;


    public AudioPlayerModule(ReactApplicationContext reactContext) {
        super(reactContext);
        mContext = reactContext;
        reactContext.addLifecycleEventListener(this);
    }

    @Override
    public String getName() {
        return "YdkAudioPlayerModule";
    }

    @Override
    public void onCatalystInstanceDestroy() {
        // PlayerManager.getInstance(mContext).onDestroy();

    }

    //播放
    @ReactMethod
    public void play(ReadableMap map, Promise promise) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject("500", "currentActivity in null");
            return;
        }
        AudioConfig audioConfig = MapUtils.toObject(map.toHashMap(), AudioConfig.class);
        if (audioConfig == null || TextUtils.isEmpty(audioConfig.getUrl())) {
            promise.reject("500", "音频地址为空");
            return;
        }
        //audioConfig.setUrl("https://m10.music.126.net/20190111180926/68230cea2c78f71b56565af46161f5a2/ymusic/363b/72ef/7661/0b373b6cdfc54e3022ef436c3ad58ec3.mp3");
        if (isLocalFile(audioConfig.getUrl())) {
            Ydk.getPermissions(currentActivity, Manifest.permission.READ_EXTERNAL_STORAGE)
                    .subscribe(grad -> {
                        if (grad) {
                            play(audioConfig, promise);
                        } else {
                            promise.reject("500", "获取权限失败");
                        }
                    }, error -> {
                        promise.reject("500", "获取权限失败");
                    });
        } else {
            play(audioConfig, promise);
        }
    }


    /**
     * 本地文件
     *
     * @param path
     * @return
     */
    private boolean isLocalFile(String path) {

        return path.startsWith("file://") || path.startsWith("/storage");
    }

    private Disposable mPlayDisposable;

    private void play(AudioConfig audioConfig, Promise promise) {

        if (mPlayDisposable != null && !mPlayDisposable.isDisposed()) {
            mPlayDisposable.dispose();
        }
        mPlayDisposable = null;

        PlayerManager.getInstance().play(audioConfig).subscribe(new Observer<AudioModelEmitter>() {
            @Override
            public void onSubscribe(Disposable d) {
                mPlayDisposable = d;
            }

            @Override
            public void onNext(AudioModelEmitter audioModelEmitter) {
                sendEvent(audioModelEmitter, promise);
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    private WritableMap greatWritableMap(AudioModel audioModel) {

        //Log.e("hh", "greatWritableMap### " + audioModel.toString());

        WritableMap params = Arguments.createMap();

        params.putDouble("tagId", audioModel.getTagId());

        double duration = audioModel.getDuration();

        params.putDouble("duration", Math.max(Math.round(duration / 1000), 1));

        double progress = audioModel.getProgress();

        params.putDouble("progress", Math.round(progress / 1000));

        double playableDuration = audioModel.getPlayableDuration();

        params.putDouble("playableDuration", Math.round(duration * playableDuration / 100 / 1000));

        return params;
    }


    @ReactMethod
    public void pause(ReadableMap map) {
        double tagId = map.getDouble("tagId");
        AudioConfig audioConfig = new AudioConfig();
        audioConfig.setTagId(tagId);
        PlayerManager.getInstance().pause(audioConfig);


    }

    @ReactMethod
    public void resume(ReadableMap map) {
        double tagId = map.getDouble("tagId");
        AudioConfig audioConfig = new AudioConfig();
        audioConfig.setTagId(tagId);
        PlayerManager.getInstance().resume(audioConfig);


    }

    @ReactMethod
    public void stop(ReadableMap map) {
        double tagId = map.getDouble("tagId");
        AudioConfig audioConfig = new AudioConfig();
        audioConfig.setTagId(tagId);
        PlayerManager.getInstance().stop(audioConfig).subscribe(audioModelEmitter -> {
            sendEvent(audioModelEmitter, null);
        });


    }

    @ReactMethod
    public void seekToTime(ReadableMap map) {
        int time = map.getInt("time");
        double tagId = map.getDouble("tagId");
        AudioConfig audioConfig = new AudioConfig();
        audioConfig.setPosstion(time);
        audioConfig.setTagId(tagId);
        PlayerManager.getInstance().seekToPosition(audioConfig);

    }

    private void sendEvent(AudioModelEmitter audioModelEmitter, Promise promise) {
        String action = audioModelEmitter.action;
        AudioModel audioModel = audioModelEmitter.audioModel;
        switch (action) {
            case Constant.BUFFER:
                sendEvent("OnAudioProgress", greatWritableMap(audioModel));

                double playableDuration = audioModel.getPlayableDuration();

                double progress = audioModel.getProgress();

                double duration = audioModel.getDuration();

                double currentBuff = playableDuration * duration / 100;

                if (currentBuff > progress) {
                    sendEvent("OnAudioLoadEnd", greatWritableMap(audioModel));
                } else {
                    sendEvent("OnAudioLoad", greatWritableMap(audioModel));
                }
                break;
            case Constant.PREPARED:
                //取消事件OnReadyToPlay
                //缓冲完成，发送一次
                if (promise != null) {
                    promise.resolve(null);
                }
                WritableMap paramsPREPARED = Arguments.createMap();
                paramsPREPARED.putDouble("tagId", audioModel.getTagId());
                sendEvent("OnReadyToPlay", paramsPREPARED);

                break;
            case Constant.ERROR:
                WritableMap paramsReeor = Arguments.createMap();
                paramsReeor.putDouble("tagId", audioModel.getTagId());
                if (audioModel.getCode() != 0) {
                    paramsReeor.putDouble("code", audioModel.getCode());
                }
                sendEvent("OnAudioError", paramsReeor);
                break;
            case Constant.PROGRESS:
                sendEvent("OnAudioProgress", greatWritableMap(audioModel));
                break;
            case Constant.COMPLETION:
                sendEvent("OnAudioEnd", greatWritableMap(audioModel));
                break;
            case Constant.BACKSTALLED:
                WritableMap paramsStalled = Arguments.createMap();
                paramsStalled.putDouble("tagId", audioModel.getTagId());
                sendEvent("OnPlaybackStalled", paramsStalled);
                break;
        }
    }


    private void sendEvent(String eventName, WritableMap map) {
        getReactApplicationContext().getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, map);
    }


    @Override
    public void onHostResume() {
        // PlayerManager.getInstance(getCurrentActivity()).resume();
    }

    @Override
    public void onHostPause() {
        // PlayerManager.getInstance(getCurrentActivity()).pause();
    }

    @Override
    public void onHostDestroy() {

    }
}

