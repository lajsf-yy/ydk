package ydk.core.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ObjectUtils {
    public static boolean isPrimitiveType(Object obj) {
        return obj == null
                || obj instanceof Boolean
                || obj instanceof Byte
                || obj instanceof Short
                || obj instanceof Integer
                || obj instanceof Long
                || obj instanceof String
                || obj instanceof Float;

    }

    public static Object toBeanObject(Object obj) {
        if (isPrimitiveType(obj)) return obj;
        if (obj instanceof List) return toListMap((List)obj );
        return toMap(obj);

    }

    private static Map<String, Object> toMap(Object obj) {
        String json = JsonUtils.stringify(obj);
        return JsonUtils.toMap(json);
    }

    private static List<Object> toListMap(List obj) {
        List<Object> list = new ArrayList<>();

        for (Object item : obj) {
            if (isPrimitiveType(item)) {
                list.add(item);
            }else if(item.getClass().isEnum()){
                list.add(item.toString());
            } else if (item instanceof Map) {
                list.add(toMap(obj));
            } else if (item instanceof List) {
                list.add(toListMap((List) item));
            }
        }
        return list;

    }
}
