package ydk.core.utils;

import com.google.gson.Gson;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapUtils {
    public static <T> T toObject(Map map, Class<T> clazz) {
        Gson gson = new Gson();
        return gson.fromJson(gson.toJsonTree(map), clazz);
    }

    public static HashMap toHashMap(Object object) {
        Gson gson = new Gson();
        HashMap hashMap = gson.fromJson(gson.toJsonTree(object), HashMap.class);
        return hashMap;
    }

    public static Map merge(Map<String, Object> sourceMap, Map<String, Object> mergeMap) {
        for (Map.Entry<String, Object> entry : mergeMap.entrySet()) {
            Object value = entry.getValue();
            String key = entry.getKey();
            if (value instanceof Integer) {
                sourceMap.put(key, value);
            } else if (value instanceof Long) {
                sourceMap.put(key, value);
            } else if (value instanceof Float) {
                sourceMap.put(key, value);
            } else if (value instanceof Double) {
                sourceMap.put(key, value);
            } else if (value instanceof String) {
                sourceMap.put(key, value);
            } else if (value instanceof Boolean) {
                sourceMap.put(key, value);
            } else if (value instanceof Array) {
                sourceMap.put(key, value);
            } else if (value instanceof List) {
                sourceMap.put(key, value);
            } else if (value instanceof Map) {
                if (sourceMap.containsKey(key)) {
                    sourceMap.put(key, merge((Map) sourceMap.get(key), (Map<String, Object>) value));
                } else {
                    sourceMap.put(key, value);
                }
            } else if (value instanceof Object) {
                sourceMap.put(key, value);
            }
        }
        return sourceMap;
    }
}
