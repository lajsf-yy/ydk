package ydk.react;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import ydk.core.YdkEventEmitter;

public class YdkReactEventEmitter implements YdkEventEmitter {
    public YdkReactEventEmitter(ReactApplicationContext reactContext) {
        mReactContext = reactContext;
    }

    ReactApplicationContext mReactContext;

    @Override
    public void emit(String eventName, Object data) {
        Object reactData = ReactUtils.toReactData(data);
        mReactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, reactData);
    }
}
