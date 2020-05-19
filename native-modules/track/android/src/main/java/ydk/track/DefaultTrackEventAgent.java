package ydk.track;


import com.zhuge.analysis.stat.ZhugeSDK;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

import ydk.core.Ydk;
import ydk.core.YdkConfigManager;

/**
 * Created by Gsm on 2018/6/29.
 */
public class DefaultTrackEventAgent implements TrackEventAgent {

    private TrackConfig trackConfig = YdkConfigManager.getConfig(TrackConfig.class);

    @Override
    public boolean track(String eventName, JSONObject jsonObject) {
        try {
            jsonObject.put("app", trackConfig.getName());
        } catch (JSONException e) {
        }
        ZhugeSDK.getInstance().track(Ydk.getApplicationContext(), eventName, jsonObject);
        return true;
    }

    @Override
    public boolean startTrack(String eventName) {

        ZhugeSDK.getInstance().startTrack(eventName);
        return true;
    }

    @Override
    public boolean endTrack(String eventName, JSONObject jsonObject) {
        ZhugeSDK.getInstance().endTrack(eventName, jsonObject);
        return true;
    }

    @Override
    public boolean identify(String uid, JSONObject jsonObject) {
        ZhugeSDK.getInstance().identify(Ydk.getApplicationContext(), uid, jsonObject);
        return true;
    }
}
