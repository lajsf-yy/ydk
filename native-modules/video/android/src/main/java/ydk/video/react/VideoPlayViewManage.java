package ydk.video.react;

import android.view.ViewGroup;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.common.MapBuilder;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.UIManagerModule;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.events.EventDispatcher;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import ydk.react.ReactUtils;
import ydk.video.VideoPlay;
import ydk.video.VideoPlayLayout;
import ydk.video.VideoPlayListener;
import ydk.video.data.VideoPlayerSource;

public class VideoPlayViewManage extends SimpleViewManager<VideoPlayLayout> {

    private static final int COMMAND_START = 1;

    private static final int COMMAND_PAUSE = 2;

    private static final int COMMAND_SEEK_TIME = 3;

    private EventDispatcher eventDispatcher;

    private VideoPlay videoPlay;

    @Nonnull
    @Override
    public String getName() {

        return "YdkVideoPlayView";
    }

    @Nonnull
    @Override
    protected VideoPlayLayout createViewInstance(@Nonnull ThemedReactContext reactContext) {

        eventDispatcher = reactContext.getNativeModule(UIManagerModule.class).getEventDispatcher();
        VideoPlayLayout playView = new VideoPlayLayout(reactContext.getCurrentActivity());
        playView.setLayoutParams(new ViewGroup.LayoutParams(-1, -1));
        playView.setPlayListener(new VideoPlayListener() {

            @Override
            public void progress(long progress, long duration) {
                WritableMap map = Arguments.createMap();
                map.putDouble("progress", progress);
                map.putDouble("duration", duration);
                dispatchEvent(playView.getId(), VideoResultEvent.videoProgress, map);
            }

            @Override
            public void buffering() {
                WritableMap map = Arguments.createMap();
                dispatchEvent(playView.getId(), VideoResultEvent.videoLoad, map);

            }

            @Override
            public void ready() {
                WritableMap map = Arguments.createMap();
                dispatchEvent(playView.getId(), VideoResultEvent.videoLoadEnd, map);
            }

            @Override
            public void stalled() {
            }

            @Override
            public void end() {
                WritableMap map = Arguments.createMap();
                dispatchEvent(playView.getId(), VideoResultEvent.playEnd, map);
            }

            @Override
            public void close() {
            }

            @Override
            public void error(Exception e) {
                WritableMap map = Arguments.createMap();
                dispatchEvent(playView.getId(), VideoResultEvent.playError, map);
            }
        });
        videoPlay = new VideoPlay(reactContext.getCurrentActivity());
        return playView;


    }


    @Override
    public void onDropViewInstance(@Nonnull VideoPlayLayout view) {
        view.onDestroyed();
        super.onDropViewInstance(view);
    }

    @Override
    public Map getExportedCustomDirectEventTypeConstants() {

        return MapBuilder.of(
                VideoResultEvent.readyToPlay, MapBuilder.of("registrationName", "onReadyToPlay"),
                VideoResultEvent.videoLoad, MapBuilder.of("registrationName", "onVideoLoad"),
                VideoResultEvent.videoLoadEnd, MapBuilder.of("registrationName", "onVideoLoadEnd"),
                VideoResultEvent.playEnd, MapBuilder.of("registrationName", "onPlayEnd"),
                VideoResultEvent.playError, MapBuilder.of("registrationName", "onPlayError"),
                VideoResultEvent.videoProgress, MapBuilder.of("registrationName", "onVideoProgress")
        );
    }

    @Override
    public Map<String, Integer> getCommandsMap() {
        return MapBuilder.of("start", COMMAND_START,
                "pause", COMMAND_PAUSE,
                "seekToTime", COMMAND_SEEK_TIME);
    }

    @Override
    public void receiveCommand(@Nonnull VideoPlayLayout root, int commandId, @Nullable ReadableArray args) {
        switch (commandId) {
            case COMMAND_START:
                root.startPlay();
                break;
            case COMMAND_PAUSE:
                root.pausePlay();
                break;
            case COMMAND_SEEK_TIME:
                root.seekTo(args.getInt(0));
                break;
        }
    }

    @ReactProp(name = "source")
    public void source(VideoPlayLayout view, ReadableMap readableMap) {

        VideoPlayerSource videoPlayerSource = ReactUtils.mapToObject(readableMap, VideoPlayerSource.class);
        videoPlay.setVideoUrl(view, videoPlayerSource.getUri());

    }


    private void dispatchEvent(int id, String action, WritableMap writableMap) {

        VideoResultEvent videoResultEvent = new VideoResultEvent(id, action, writableMap);

        eventDispatcher.dispatchEvent(videoResultEvent);

    }

}
