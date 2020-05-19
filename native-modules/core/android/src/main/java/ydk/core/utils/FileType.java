package ydk.core.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Gsm on 2018/6/7.
 */
class FileType {
    private static HashMap<String, String> fileTypes = new HashMap();

    static {
        fileTypes.put("FFD8FF", "jpg");
        fileTypes.put("89504E47", "png");
        fileTypes.put("47494638", "gif");
        fileTypes.put("49492A00", "tif");
        fileTypes.put("424D", "bmp");
        fileTypes.put("41433130", "dwg");
        fileTypes.put("38425053", "psd");
        fileTypes.put("7B5C727466", "rtf");
        fileTypes.put("3C3F786D6C", "xml");
        fileTypes.put("68746D6C3E", "html");
        fileTypes.put("44656C69766572792D646174653A", "eml");
        fileTypes.put("D0CF11E0", "doc");
        fileTypes.put("5374616E64617264204A", "mdb");
        fileTypes.put("252150532D41646F6265", "ps");
        fileTypes.put("255044462D312E", "pdf");
        fileTypes.put("504B0304", "zip");
        fileTypes.put("52617221", "rar");
        fileTypes.put("57415645", "wav");
        fileTypes.put("41564920", "avi");
        fileTypes.put("2E524D46", "rm");
        fileTypes.put("000001BA", "mp4");
        fileTypes.put("000001B3", "mp4");
        fileTypes.put("6D6F6F76", "mov");
        fileTypes.put("3026B2758E66CF11", "asf");
        fileTypes.put("4D546864", "mid");
        fileTypes.put("1F8B08", "gz");
    }

    String getType(String key) {
        for (Map.Entry<String, String> entry : fileTypes.entrySet()) {
            if (entry.getKey().contains(key))
                return entry.getValue();
        }
        return "";
    }


}
