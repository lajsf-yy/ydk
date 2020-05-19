package ydk.track;

import org.json.JSONObject;

import java.util.Map;

/**
 * Created by Gsm on 2018/4/17.
 */

public interface TrackEventAgent {

    boolean track(String eventName, JSONObject jsonObject);

    boolean startTrack(String eventName);

    boolean endTrack(String eventName, JSONObject jsonObject);

    boolean identify(String uid, JSONObject jsonObject);
}
