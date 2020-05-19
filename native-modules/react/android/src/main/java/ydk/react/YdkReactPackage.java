package ydk.react;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.Collections;
import java.util.List;

import ydk.core.Ydk;
import ydk.react.image.YdkImageViewManager;


public class YdkReactPackage implements ReactPackage {
    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        YdkReactEventEmitter eventEmitter = new YdkReactEventEmitter(reactContext);
        Ydk.setEventEmitter(eventEmitter);
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Collections.<ViewManager>singletonList(new YdkImageViewManager());
    }
}
