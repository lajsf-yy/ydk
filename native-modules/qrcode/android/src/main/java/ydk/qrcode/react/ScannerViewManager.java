package ydk.qrcode.react;

import android.util.Log;

import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.EventDispatcher;

import java.util.Map;

import javax.annotation.Nullable;

import ydk.core.utils.MapUtils;
import ydk.qrcode.scanner.CaptureView;
import ydk.qrcode.scanner.ScannerConfig;


public class ScannerViewManager extends SimpleViewManager<CaptureView> {

    private static final int COMMAND_FLASH_LIGHT = 1;

    private EventDispatcher eventDispatcher;

    @Override
    public String getName() {
        return "YdkScannerView";
    }

    @Override
    protected CaptureView createViewInstance(ThemedReactContext reactContext) {
        CaptureView CaptureView = new CaptureView(reactContext.getCurrentActivity());
        CaptureView.setScannerResultListener(result -> {

                    eventDispatcher.dispatchEvent(new ScannerResultEvent(CaptureView.getId(), result));
                }
        );
        eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
        return CaptureView;
    }

    @ReactProp(name = "config")
    public void setConfig(CaptureView view, ReadableMap map) {

        ScannerConfig config = MapUtils.toObject(map.toHashMap(), ScannerConfig.class);

        view.setScannerConfig(config);
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of("setFlashLight", COMMAND_FLASH_LIGHT);
    }

    @Override
    public void receiveCommand(CaptureView view, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case COMMAND_FLASH_LIGHT:
                view.turnOnFlashLight();
                break;
        }
    }

    @Override
    public Map<String, Object> getExportedCustomDirectEventTypeConstants() {
        return MapBuilder.of(ScannerResultEvent.EVENT_NAME, MapBuilder.of("registrationName", "onScannerResult"));
    }

    @Override
    public void onDropViewInstance(CaptureView view) {
        super.onDropViewInstance(view);
        view.onDestroy();
    }

}
