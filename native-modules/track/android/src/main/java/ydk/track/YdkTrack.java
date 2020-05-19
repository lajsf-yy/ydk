package ydk.track;

import android.content.Context;


import com.zhuge.analysis.stat.ZhugeSDK;

import org.json.JSONObject;

import java.util.Map;

import io.reactivex.Observable;
import ydk.annotations.YdkModule;

@YdkModule(export = true)
public class YdkTrack {

    private Context context;

    private TrackEventAgent mTrackEventAgent = new DefaultTrackEventAgent();

    public YdkTrack(Context context) {
        this.context = context;
        ZhugeSDK.getInstance().init(context);
    }


    public void setTrackEventAgent(TrackEventAgent trackEventAgent) {
        this.mTrackEventAgent = trackEventAgent;
    }

    public TrackEventAgent getTrackEventAgent() {

        return mTrackEventAgent;
    }


    public Observable<Boolean> setEvent(String eventName, Map<String, Object> map) {
        mTrackEventAgent.track(eventName, new JSONObject(map));
        return Observable.just(true);
    }


    public Observable<Boolean> startTrack(String eventName) {
        mTrackEventAgent.startTrack(eventName);
        return Observable.just(true);
    }


    public Observable<Boolean> endTrack(String eventName, Map<String, Object> map) {
        mTrackEventAgent.endTrack(eventName, new JSONObject(map));
        return Observable.just(true);
    }

    public Observable<Boolean> identify(String uid, Map<String, Object> map) {
        mTrackEventAgent.identify(uid, new JSONObject(map));
        return Observable.just(true);
    }
}
