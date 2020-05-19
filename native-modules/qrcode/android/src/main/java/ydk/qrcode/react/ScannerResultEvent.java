package ydk.qrcode.react;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

/**
 * Created by Gsm on 2018/5/3.
 */
class ScannerResultEvent extends Event<ScannerResultEvent> {

    public static final String EVENT_NAME = "scannerResult";
    private String result;

    ScannerResultEvent(int viewTag, String result) {
        super(viewTag);
        this.result = result;
    }

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), serializeEventData());
    }

    private WritableMap serializeEventData() {
        WritableMap eventData = Arguments.createMap();
        eventData.putString("codeInfo", result);
        return eventData;
    }
}
