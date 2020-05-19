package ydk.core;

import com.google.gson.*;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import ydk.annotations.YdkConfigNode;
import ydk.annotations.YdkConfigValue;
import ydk.core.utils.StringUtils;


 public  class YdkConfigManager {
     private  static JsonObject config;
     private static Map<Class<?>,Object> configCache=new HashMap<>();
     static void setup(String ydkConfig) {
         config = new JsonParser().parse(ydkConfig).getAsJsonObject();
     }

     public static <T> T getConfig(Class<T> cls) {
         if(configCache.containsKey(cls))
             return (T)configCache.get(cls);

         YdkConfigNode ydkConfigNode = cls.getAnnotation(YdkConfigNode.class);
         assert ydkConfigNode != null;
         T t =  getConfig(cls, ydkConfigNode.name());
         configCache.put(cls, t);
         return t;
     }

     private static JsonElement getJsonElement(String path) {
         String[] paths = path.split("\\.");
         JsonObject ele = config;
         for(int i=0;i<paths.length;i++){
             if(i==paths.length-1)
                 return ele.get(paths[i]);
             else
             ele=ele.getAsJsonObject(paths[i]);
         }
         return  ele;

     }

     static <T> T getConfig(Class<T> cls, String nodeName) {

         JsonObject node;
         if (StringUtils.isEmpty(nodeName)) {
             node = new JsonObject();
         } else {
             node = config.getAsJsonObject(nodeName);
         }
         Field[] fields = cls.getDeclaredFields();
         for (Field field : fields) {
             YdkConfigValue ydkConfigValue = field.getAnnotation(YdkConfigValue.class);
             if (ydkConfigValue == null) {
                 continue;
             }
             String configName = ydkConfigValue.name();
             JsonElement value = getJsonElement(configName);
             if (value == null)
                 continue;
             if (field.getType() == String.class) {
                 node.addProperty(field.getName(), value.getAsString());
             } else if (field.getType() == Boolean.class) {
                 node.addProperty(field.getName(), value.getAsBoolean());
             } else {
                 node.addProperty(field.getName(), value.getAsFloat());
             }

         }
         return new Gson().fromJson(node, cls);

     }
 }
