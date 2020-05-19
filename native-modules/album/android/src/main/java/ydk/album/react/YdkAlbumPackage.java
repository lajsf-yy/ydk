package ydk.album.react;

/**
 * Created by Administrator on 2018/1/10.
 */


import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;


import java.util.Arrays;
import java.util.List;



public class YdkAlbumPackage implements ReactPackage {

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        List<NativeModule> nativeModules = Arrays.<NativeModule>asList(
                new YdkAlbumModule(reactContext));
        return nativeModules;
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        List<ViewManager> viewManagers = Arrays.<ViewManager>asList();
        return viewManagers;
    }
}