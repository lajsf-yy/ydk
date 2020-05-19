package ydk.video.react;

import com.facebook.react.bridge.WritableMap;
import com.facebook.react.uimanager.events.Event;
import com.facebook.react.uimanager.events.RCTEventEmitter;

public class VideoResultEvent extends Event<VideoResultEvent> {

    public static final String readyToPlay = "readyToPlay";
    public static final String videoLoad = "readyToPlay";
    public static final String videoLoadEnd = "readyToPlay";
    public static final String playEnd = "readyToPlay";
    public static final String playError = "readyToPlay";
    public static final String videoProgress = "videoProgress";


    private String EVENT_NAME;

    private WritableMap writableMap;

    public VideoResultEvent(int viewTag, String eventName, WritableMap writableMap) {
        super(viewTag);
        this.EVENT_NAME = eventName;
        this.writableMap = writableMap;
    }

    @Override
    public String getEventName() {

        return EVENT_NAME;
    }

    @Override
    public void dispatch(RCTEventEmitter rctEventEmitter) {
        rctEventEmitter.receiveEvent(getViewTag(), getEventName(), writableMap);

    }
}
