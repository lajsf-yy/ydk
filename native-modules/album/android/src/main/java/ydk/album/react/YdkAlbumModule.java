package ydk.album.react;


import android.app.Activity;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.luck.picture.lib.config.PictureMimeType;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import ydk.album.MediaPickConstants;
import ydk.album.PicturePick;
import ydk.album.PicturePickConfig;
import ydk.core.utils.MapUtils;
import ydk.react.ArgumentsUtils;

/**
 * Created by Gsm on 2018/5/4.
 */
public class YdkAlbumModule extends ReactContextBaseJavaModule {

    public YdkAlbumModule(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "YdkAlbumModule";
    }

    @Nullable
    @Override
    public Map<String, Object> getConstants() {
        Map<String, Object> map = new HashMap<>();
        map.put("image", MediaPickConstants.TYPE_PICTURE);
        map.put("video", MediaPickConstants.TYPE_VIDEO);
        map.put("all", MediaPickConstants.TYPE_ALL);
        return map;
    }

    @ReactMethod
    public void photoTakenWithRecord(Double type, Promise promise) {
        Activity currentActivity = getCurrentActivity();
        new PicturePick().take(currentActivity, type.intValue()).subscribe(
                mediaInfo -> promise.resolve(ArgumentsUtils.toWritableMap(mediaInfo)),
                error -> promise.reject("500", error.getMessage()));
    }

    @ReactMethod
    public void picturePick(ReadableMap map, Promise promise) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject("500", "parameters error");
            return;
        }
        if (!map.hasKey("type")) {
            promise.reject("500", "parameters error");
            return;
        }
        int type = map.getInt("type");

        if (type != MediaPickConstants.TYPE_PICTURE && type != MediaPickConstants.TYPE_VIDEO && type != MediaPickConstants.TYPE_ALL) {
            promise.reject("500", "parameters error");
            return;
        }
        PicturePickConfig config = MapUtils.toObject(MapUtils.merge(MapUtils.toHashMap(new PicturePickConfig()),
                map.toHashMap()), PicturePickConfig.class);
        new PicturePick().pick(currentActivity, config).subscribe(
                mediaInfo -> promise.resolve(ArgumentsUtils.toWritableMap(mediaInfo)),
                error -> promise.reject("500", error.getMessage()));
    }


}
