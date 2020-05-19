package ydk.core.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class FileUtils {
    public static boolean delete(String fileName) {
        File file = new File(fileName);
        if (!file.exists()) return false;
        if (file.isFile()) return deleteFile(fileName);
        else return deleteDirectory(fileName);
    }

    public static boolean deleteFile(String fileName) {
        File file = new File(fileName);
        if (file.exists() && file.isFile()) {
            return file.delete();
        }
        return false;
    }

    public static boolean deleteDirectory(String dir) {
        dir = dir.endsWith(File.separator) ? dir : dir + File.separator;
        File dirFile = new File(dir);
        if (!dirFile.exists() || !dirFile.isDirectory()) return false;
        boolean flag = true;
        File[] files = dirFile.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                flag = deleteFile(file.getAbsolutePath());
            } else if (file.isDirectory()) {
                flag = deleteDirectory(file.getAbsolutePath());
            }
            if (!flag) break;
        }
        if (!flag) return false;
        return dirFile.delete();
    }

    public static String getSuffix(String filePath) {
        String temp = filterChinese(filePath);
        String ext = MimeTypeMap.getSingleton().getExtensionFromMimeType(getMimeType(filePath));
        if (TextUtils.isEmpty(ext)) ext = MimeTypeMap.getFileExtensionFromUrl(filePath);
        if (TextUtils.isEmpty(ext) && !TextUtils.isEmpty(temp)) {
            int lastDot = temp.lastIndexOf(".");
            if (lastDot >= 0) {
                ext = temp.substring(lastDot + 1);
            }
        }
        if (!TextUtils.isEmpty(ext)) ext = "." + ext;
        return ext;
    }

    public static String filterChinese(String chin) {
        chin = chin.replaceAll("[(\\u4e00-\\u9fa5)]", "");
        return chin;
    }

    public static String getMimeType(String filePath) {
        MediaMetadataRetriever mmr = new MediaMetadataRetriever();
        String mime = "";
        if (filePath != null) {
            try {
                mmr.setDataSource(filePath);
                mime = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE);
            } catch (RuntimeException e) {
                return mime;
            }
        }
        return mime;
    }

    public static String getFileType(String filePath) {
        String type = "";
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(filePath);
            byte[] bytes = new byte[3];
            inputStream.read(bytes, 0, bytes.length);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String aCase = Integer.toHexString(bytes[i] & 0xFF).toUpperCase();
                builder.append(aCase.length() < 2 ? 0 : aCase);
            }
            type = new FileType().getType(builder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return type;
    }

    /**
     * 修复oppo高版本手机，原方法报错
     *
     * @param filePath
     * @return
     */
    public static boolean isImage(final String filePath) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        try {
            Bitmap bitmap = BitmapFactory.decodeFile(filePath, options);
            return options.outWidth != -1 && options.outHeight != -1;
        } catch (Exception e) {
            return false;
        }
    }
//
//    public static boolean isImage(String filePath) {
//        String mimeType = getMimeType(filePath);
//        if (TextUtils.isEmpty(mimeType)) {
//            mimeType = MediaFile.getMimeTypeForFile(filePath);
//        }
//        int type = MediaFile.getFileTypeForMimeType(mimeType);
//        return 0 != type && MediaFile.isImageFileType(type);
//    }

    public static boolean isVideo(String filePath) {
        String mimeType = getMimeType(filePath);
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = MediaFile.getMimeTypeForFile(filePath);
        }
        int type = MediaFile.getFileTypeForMimeType(mimeType);
        return 0 != type && MediaFile.isVideoFileType(type);
    }

    public static boolean isAudio(String filePath) {
        String mimeType = getMimeType(filePath);
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = MediaFile.getMimeTypeForFile(filePath);
        }
        int type = MediaFile.getFileTypeForMimeType(mimeType);
        return 0 != type && MediaFile.isAudioFileType(type);
    }

    public static String getPath(Context context, String dirName) {
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

    public static File create(String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }

        File f = new File(filePath);
        if (!f.getParentFile().exists()) {// 如果不存在上级文件夹
            f.getParentFile().mkdirs();
        }
        try {
            f.createNewFile();
            return f;
        } catch (IOException e) {
            if (f != null && f.exists()) {
                f.delete();
            }
            return null;
        }
    }

    public static long copy(String srcPath, String dstPath) {
        if (TextUtils.isEmpty(srcPath) || TextUtils.isEmpty(dstPath)) {
            return -1;
        }

        File source = new File(srcPath);
        if (!source.exists()) {
            return -1;
        }

        if (srcPath.equals(dstPath)) {
            return source.length();
        }

        FileChannel fcin = null;
        FileChannel fcout = null;
        try {
            fcin = new FileInputStream(source).getChannel();
            fcout = new FileOutputStream(create(dstPath)).getChannel();
            ByteBuffer tmpBuffer = ByteBuffer.allocateDirect(4096);
            while (fcin.read(tmpBuffer) != -1) {
                tmpBuffer.flip();
                fcout.write(tmpBuffer);
                tmpBuffer.clear();
            }
            return source.length();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (fcin != null) {
                    fcin.close();
                }
                if (fcout != null) {
                    fcout.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return -1;
    }
}
