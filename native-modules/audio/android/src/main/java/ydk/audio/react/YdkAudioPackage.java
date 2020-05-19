package ydk.audio.react;

/**
 * Created by Administrator on 2018/1/10.
 */


import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;


import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Administrator on 2018/1/3.
 */

public class YdkAudioPackage implements ReactPackage {

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> nativeModules = Arrays.<NativeModule>asList(

                new AudioPlayerModule(reactContext),
                new AudioRecorderModule(reactContext)
        );
        return nativeModules;
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {

        return Collections.emptyList();
    }
}