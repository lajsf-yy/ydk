package ydk.core.utils.image;

import android.text.TextUtils;


import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ydk.core.utils.FileUtils;

/**
 * Created by Gsm on 2018/3/21.
 */

public class Checker {
    private static List<String> format = new ArrayList<>();
    private static final String JPG = "jpg";
    private static final String JPEG = "jpeg";
    private static final String PNG = "png";
    private static final String WEBP = "webp";
    private static final String GIF = "gif";
    private static final String IMAGE_JPEG = "image/jpeg";
    private static final String IMAGE_PNG = "image/png";

    static {
        format.add(JPG);
        format.add(JPEG);
        format.add(PNG);
        format.add(WEBP);
        format.add(GIF);
        format.add(IMAGE_JPEG);
        format.add(IMAGE_PNG);
    }

    static boolean isImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        String suffix = FileUtils.getSuffix(path);
        if (suffix.contains(".")) {
            suffix = suffix.replace(".", "");
        }
        return format.contains(suffix.toLowerCase());
    }

    static boolean isGif(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }
        String suffix = FileUtils.getSuffix(path);
        return suffix.contains(GIF);
    }

    static boolean isJPG(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        String suffix = path.substring(path.lastIndexOf("."), path.length()).toLowerCase();
        return suffix.contains(JPG) || suffix.contains(JPEG);
    }

    static String checkSuffix(String path) {
        if (TextUtils.isEmpty(path)) {
            return ".jpg";
        }
        return FileUtils.getSuffix(path);
    }

    static boolean isNeedCompress(int leastCompressSize, String path) {
        if (TextUtils.isEmpty(path)) return false;
        if (FileUtils.getSuffix(path).contains(GIF)) return false;
        if (leastCompressSize > 0) {
            File source = new File(path);
            if (!source.exists()) {
                return false;
            }
            if (source.length() <= (leastCompressSize << 10)) {
                return false;
            }
        }
        return true;
    }

}
