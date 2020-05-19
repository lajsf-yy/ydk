package ydk.core.utils;

import android.content.Context;

import java.io.File;

/**
 * Created by Gsm on 2018/5/2.
 */
public class CacheUtils {

    public static void clearCache(Context context) {
        FileUtils.deleteDirectory(getVideoFilePath(context));
        FileUtils.deleteDirectory(getImageFilePath(context));
        FileUtils.deleteDirectory(getExoFilePath(context));
    }

    /**
     * 视频缓存路径
     *
     * @return
     */
    public static String getVideoFilePath(Context context) {
        return getPath(context, "video");
    }
	/**
     * 语音缓存路径
     *
     * @return
     */
    public static String getAudioFilePath(Context context) {
        return getPath(context, "audio");
    }
    /**
     * 图片路径
     *
     * @return
     */
    public static String getImageFilePath(Context context) {
        return getPath(context, "image");
    }

    /**
     * exo 缓存路径
     */
    public static String getExoFilePath(Context context) {
        return getPath(context, "exo");
    }

    private static String getPath(Context context, String dirName) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, dirName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                return null;
            }
            return result.getAbsolutePath();
        }
        return "";
    }

}
