package ydk.core.utils;

import android.Manifest;
import android.app.Activity;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.UUID;

import ydk.core.Ydk;


/**
 * Created by Administrator on 2018/2/27.
 */

public class ImageUtils {

    public static final int IMAGE_MIN_SIZE = 500 * 1024;//图片默认最小为500kb

    public static Bitmap getVideoThumbnail(String filePath) {
        if (TextUtils.isEmpty(filePath)) return null;
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (retriever != null) retriever.release();
        }
        return bitmap;
    }

    public static String saveTo(Bitmap bm, String dirPath) {
        if (bm == null) return "";
        dirPath = TextUtils.isEmpty(dirPath) ? String.valueOf(System.currentTimeMillis()) : dirPath;
        //新建文件夹用于存放裁剪后的图片
        File tmpDir = new File(CacheUtils.getImageFilePath(Ydk.getApplicationContext()), dirPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        String uuid = UUID.randomUUID().toString();
        //新建文件存储裁剪后的图片
        try {
            String localFile = tmpDir.getAbsolutePath() + "/" + uuid + ".png";
            //打开文件输出流
            FileOutputStream fos = new FileOutputStream(localFile);
            //将bitmap压缩后写入输出流(参数依次为图片格式、图片质量和输出流)
            bm.compress(Bitmap.CompressFormat.PNG, 100, fos);
            //刷新输出流
            fos.flush();
            //关闭输出流
            fos.close();
            //返回File类型的Uri
            return localFile;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 更新相册
     *
     * @param context
     * @param filePath
     * @param isImage
     */
    public static void updatePhotoAlbum(Context context, String filePath, boolean isImage) {
        try {
            if (isImage) {
                MediaStore.Images.Media.insertImage(context.getContentResolver(), filePath, new File(filePath).getName(), null);
            }
            context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + filePath)));
            //4.4
            MediaScannerConnection.scanFile(context, new String[]{filePath}, null, null);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }



    /**
     * @param data
     * @return
     */
    public static Bitmap getBitmapBase64(String data) {
        if (data.contains(",") && data.split(",").length == 2)
            data = data.split(",")[1];
        byte[] decode = Base64.decode(data, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decode, 0, decode.length);
    }

    /**
     * 旋转图片
     *
     * @param bitmap
     * @param srcExif
     * @return
     */
    public static Bitmap rotatingImage(Bitmap bitmap, ExifInterface srcExif) {
        if (srcExif == null || bitmap == null) return bitmap;
        Matrix matrix = new Matrix();
        int angle = 0;
        int orientation = srcExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                angle = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                angle = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                angle = 270;
                break;
        }
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }

}
