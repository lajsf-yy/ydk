package ydk.navigation.react;

import android.os.Bundle;

import androidx.annotation.Nullable;

import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;

import org.greenrobot.eventbus.EventBus;

import javax.annotation.Nonnull;

public class NativeRouterModule extends ReactContextBaseJavaModule {

    private final String NAME = "YdkNavigationModule";


    public NativeRouterModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
    }


    @Nonnull
    @Override
    public String getName() {
        return NAME;
    }

//    public void onLoginSuccess() {
//        ((RNPageActivity)getCurrentActivity()).onLoginSuccess();
//    }

//    @ReactMethod
//    public void push(String componentName, String componentId, Promise promise) {
//        this.pushWithOptions(componentName, componentId, null, promise);
//    }

    @ReactMethod
    public void registerComponents(ReadableArray components, Promise promise) {
    }


    @ReactMethod
    public void push(String componentId, @Nullable ReadableMap options, Promise promise) {

        ReadableMap props = null;
        if (options.hasKey("passProps")) {
            props = options.getMap("passProps");
        }

        if (getCurrentActivity() == null) {
            Long current = System.currentTimeMillis();
            if (current - lastPushTime > 600) {//comment out as open different page immediately.
                Bundle bundle = Arguments.toBundle(props);
                bundle.putString("componentId", options.getString("componentId"));
                PushOptionsData data = new PushOptionsData(options.getString("componentName"),
                        options.getString("componentId"), bundle);
                EventBus.getDefault().post(data);
                lastPushTime = current;
            }
            promise.resolve("100");
            return;
        }

        if (interceptIfNeeded(options.getString("componentName"), props)) {
            return;
        }

        this.push(options.getString("componentName"),
                options.getString("componentId"), props, promise);
    }


    private boolean interceptIfNeeded(String name, ReadableMap props) {
        if (NativePageLinker.getInstance().isNativePages(getCurrentActivity(), name)) {
            NativePageLinker.getInstance().directOpen(getCurrentActivity(),
                    name, LocalArguments.toBundle(props));
            return true;
        }
//        for (String page:nativePages) {
//            if (page.equalsIgnoreCase(name)) {
//                NativePageLinker.getInstance().directOpen(getCurrentActivity(),
//                        name, LocalArguments.toBundle(props));
//                return true;
//            }
//        }
        return false;
    }

    private Long lastPushTime = 0L;

    public void push(String componentName, String componentId, @Nullable ReadableMap options, Promise promise) {

//        Log.i("XXX", "componentName is " + componentName + " componentId is " + componentId);
        try {
            Bundle bundle = Arguments.toBundle(options);
//            Bundle bundle = new Bundle();
//            try {
//                if (null != options) {
//                    HashMap<String, Object> map = options.toHashMap();
//                    for (String key: map.keySet()) {
//                        Object value = map.get(key);
////                        Log.i("XXX", "key is " + key + " value is " + value);
//                        if (value  instanceof String) {
//                            bundle.putString(key, (String) value);
//                        } else if (value instanceof  Integer || value instanceof Float || value instanceof Long || value instanceof Double) {
//                            bundle.putDouble(key, (Double) value);
//                        }
//    //                bundle.put(key, value);
//                    }
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            bundle.putString("componentId", componentId);
            Long current = System.currentTimeMillis();
//            Log.i("xxx", "time gap is "+(current - lastPushTime));
            if (current - lastPushTime > 600) {//comment out as open different page immediately.
                ((IRouter) getCurrentActivity()).push(componentName, componentId, bundle);
                lastPushTime = current;
            }
            promise.resolve("200");
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject("300", e.getMessage());
        }
    }

    @ReactMethod
    public void pop(String componentId, Promise promise) {

        try {
            ((IRouter) getCurrentActivity()).pop(componentId);
            promise.resolve("200");
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject("300", e.getMessage());
        }
    }

    @ReactMethod
    public void popTo(String componentId, Promise promise) {

        try {
            ((IRouter) getCurrentActivity()).popTo(componentId);
            promise.resolve("200");
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject("300", e.getMessage());
        }
    }

    @ReactMethod
    public void popToRoot(String component, Promise promise) {
        popToRoot(promise);
    }

    public void popToRoot(Promise promise) {

        try {
            ((IRouter) getCurrentActivity()).popToRoot();
            promise.resolve("200");
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject("300", e.getMessage());
        }
    }

    @ReactMethod
    public void popToRootAndSwitchTab(String component, Integer tabIndex) {
        try {
            ((IRouter) getCurrentActivity()).popToRoot(tabIndex);
//            promise.resolve("200");
        } catch (Exception e) {
            e.printStackTrace();
//            promise.reject("300", e.getMessage());
        }
    }

    @ReactMethod
    public void showModal(String componentId, @Nullable ReadableMap options, Promise promise) {
        ReadableMap props = null;
        if (options.hasKey("passProps")) {
            props = options.getMap("passProps");
        }
        if (getCurrentActivity() == null) {
            ModalOptionsData data = new ModalOptionsData(options.getString("componentName"), props);
            EventBus.getDefault().post(data);
            promise.resolve("100");
            return;
        }
        this.showModal(options.getString("componentName"), options.getString("componentId"), props, promise);
    }

    public void showModal(String componentName, String componentId, @Nullable ReadableMap options, Promise promise) {
//        ((RNPageActivity)getCurrentActivity()).showModalWithComponent(componentId);
//        promise.reject("300", "not implementation");
        try {
            ((IRouter) getCurrentActivity()).showModal(componentName, options);
            promise.resolve("200");
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject("300", e.getMessage());
        }
    }

    @ReactMethod
    public void dismissModal(String componentId, Promise promise) {
        this.dismissModal(promise);
    }

    public void dismissModal(Promise promise) {

        try {
            ((IRouter) getCurrentActivity()).dismissModal();
            promise.resolve("200");
        } catch (Exception e) {
            e.printStackTrace();
            promise.reject("300", e.getMessage());
        }
    }

    @ReactMethod
    public void setResult(String componentId, String targetComponentId, ReadableMap data) {
//        WritableMap map = Arguments.createMap();
//        map.putString("componentId",componentId);
//        map.putMap("data",Arguments.makeNativeMap(data.toHashMap()));
//        getReactApplicationContext()
//                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit("ComponentReceiveResult", map);

		try {
            ((IRouter) getCurrentActivity()).setResult(componentId, targetComponentId, data);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }

}
