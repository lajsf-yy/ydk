package ydk.react;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeMap;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import ydk.core.utils.MapUtils;

/**
 * Created by Gsm on 2018/4/20.
 */
public class ArgumentsUtils {

    public static WritableArray toWritableArray(List list) {
        WritableArray writableArray = Arguments.createArray();
        if (list != null && !list.isEmpty()) {
            for (Object obj : list) {
                if (obj == null) {
                    writableArray.pushNull();
                } else if (obj instanceof Boolean) {
                    writableArray.pushBoolean((Boolean) obj);
                } else if (obj instanceof Integer) {
                    writableArray.pushInt((Integer) obj);
                } else if (obj instanceof Double) {
                    writableArray.pushDouble((Double) obj);
                } else if (obj instanceof String) {
                    writableArray.pushString((String) obj);
                } else if (obj instanceof List) {
                    writableArray.pushArray(toWritableArray((List) obj));
                } else if (obj instanceof Array) {
                    writableArray.pushArray(toWritableArray(Arrays.asList(obj)));
                } else if (obj instanceof WritableNativeMap) {
                    writableArray.pushMap((WritableMap) obj);
                } else if (obj instanceof WritableArray) {
                    writableArray.pushArray((WritableArray) obj);
                } else {
                    writableArray.pushMap(Arguments.makeNativeMap(MapUtils.toHashMap(obj)));
                }
            }
        }
        return writableArray;
    }

    public static WritableMap toWritableMap(Object obj) {
        WritableMap writableMap = Arguments.createMap();
        if (obj != null) {
            Map<String, Object> sourceMap;
            if (obj instanceof Map) {
                sourceMap = (Map) obj;
            } else {
                sourceMap = MapUtils.toHashMap(obj);
            }
            for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (value == null) {
                    writableMap.putNull(key);
                } else if (value instanceof Boolean) {
                    writableMap.putBoolean(key, (Boolean) value);
                } else if (value instanceof Integer) {
                    writableMap.putInt(key, (Integer) value);
                } else if (value instanceof Double) {
                    writableMap.putDouble(key, (Double) value);
                } else if (value instanceof String) {
                    writableMap.putString(key, (String) value);
                } else if (value instanceof List) {
                    writableMap.putArray(key, toWritableArray((List) value));
                } else if (value instanceof Array) {
                    writableMap.putArray(key, toWritableArray(Arrays.asList(obj)));
                } else if (value instanceof WritableNativeMap) {
                    writableMap.putMap(key, (WritableMap) value);
                } else if (value instanceof WritableArray) {
                    writableMap.putArray(key, (WritableArray) value);
                } else {
                    writableMap.putMap(key, Arguments.makeNativeMap(MapUtils.toHashMap(value)));
                }
            }
        }
        return writableMap;
    }

}
