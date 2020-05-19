package ydk.video.react;

import android.app.Activity;
import android.text.TextUtils;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import javax.annotation.Nonnull;

import ydk.core.utils.MapUtils;
import ydk.video.YdkVideo;
import ydk.video.data.NativeVideoData;

public class YdkVideoModule extends ReactContextBaseJavaModule {

    public YdkVideoModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Nonnull
    @Override
    public String getName() {
        return "YdkVideoModule";
    }

    @ReactMethod
    public void nativePlay(ReadableMap map) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            return;
        }
        NativeVideoData nativeVideoData = MapUtils.toObject(map.toHashMap(), NativeVideoData.class);
        if (nativeVideoData == null || TextUtils.isEmpty(nativeVideoData.getUri())) {
            return;
        }
        currentActivity.runOnUiThread(
                () -> YdkVideo.nativePlay(currentActivity, nativeVideoData.getUri()));
    }

}
