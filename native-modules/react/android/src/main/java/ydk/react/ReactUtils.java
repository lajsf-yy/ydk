package ydk.react;


import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Dynamic;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableType;
import com.facebook.react.bridge.WritableMap;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import ydk.core.utils.MapUtils;
import ydk.core.utils.ObjectUtils;
import ydk.react.error.ResultException;

public class ReactUtils {

    public static String dynamicToJSON(Dynamic dynamic) {
        String josn = "";
        if (dynamic == null) {
            return "";
        }
        ReadableType type = dynamic.getType();
        switch (type) {
            case Map:
                josn = new JSONObject(dynamic.asMap().toHashMap()).toString();
                break;
            case Array:
                josn = new JSONArray(dynamic.asArray().toArrayList()).toString();
                break;
            case Number:
                josn = String.valueOf(dynamic.asDouble());
                break;
            case String:
                josn = dynamic.asString();
                break;
            case Boolean:
                josn = String.valueOf(dynamic.asBoolean());
                break;
            default:
                josn = "";
        }
        return josn;
    }

    public static <T> T mapToObject(ReadableMap map, Class<T> clazz) {
        return MapUtils.toObject(map.toHashMap(), clazz);
    }

    public static Object toReactData(Object obj) {
        if (ObjectUtils.isPrimitiveType(obj)) {
            if (obj == null) {
                return null;
            }
            if (obj instanceof Double) {
                return Double.valueOf(obj.toString());
            }
            return obj;
        }
        Object value = ObjectUtils.toBeanObject(obj);
        if (value instanceof List) {
            return Arguments.makeNativeArray(((List) value).toArray());
        } else if (value instanceof Arrays) return Arguments.makeNativeArray(value);
        Map map = (Map) value;
        return Arguments.makeNativeMap(map);

    }

    public static <T> void subscribe(Observable<T> observable, Promise promise) {
        subscribe(observable, promise, "500");
    }

    @SuppressWarnings({"unchecked"})
    public static <T> void subscribe(Observable<T> observable, Promise promise, String errorCode) {
        Disposable disposable = observable.subscribe(obj -> {
                    Object data = toReactData(obj);
                    promise.resolve(data);
                }
                , err -> {
                    if (err instanceof ResultException) {
                        ResultException resultException = (ResultException) err;
                        Object object = resultException.getObject();
                        WritableMap map = Arguments.createMap();
                        if (object != null) {
                            HashMap hashMap = MapUtils.toHashMap(object);
                            map = Arguments.makeNativeMap(hashMap);
                        }
                        promise.reject(resultException.getCode(), resultException.getMessage(), err, map);
                    } else {
                        promise.reject(errorCode, err);
                    }

                });


    }


}
