package ydk.core.utils;

import com.google.gson.Gson;

import com.google.gson.reflect.TypeToken;


import java.util.List;
import java.util.Map;


/**
 * Created by Gsm on 2018/4/10.
 */

public class JsonUtils {

    public static <T> T toObject(String json, Class<T> cls) {
        return new Gson().fromJson(json, cls);

    }

    public static <T> List<T> toList(String json, Class<T> cls) {

        return new Gson().fromJson(json, TypeToken.getArray(cls.getComponentType()).getType());
    }

    public static Map<String, Object> toMap(String json) {

        return new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {
        }.getType());
    }

    public static String stringify(Object obj) {
        return new Gson().toJson(obj);
    }

    public static Map<String, String> toStrMap(String json) {

        return new Gson().fromJson(json, new TypeToken<Map<String, String>>() {
        }.getType());
    }

}
