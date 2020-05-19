package ydk.core.utils.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import ydk.core.utils.ImageUtils;

/**
 * Created by Gsm on 2018/3/21.
 */

public class CompressCore {
    private ExifInterface mSrcExif;
    private String mSrcImgPath;
    private File mTargetImgFile;
    private int mSrcWidth;
    private int mSrcHeight;
    private int mTargetMinSize;

    public CompressCore(String srcImgPath, File targetImgFile, int targetMinSize) throws IOException {
        if (Checker.isJPG(srcImgPath)) {//exif 不支持 PNG、WebP、gif
            mSrcExif = new ExifInterface(srcImgPath);
        }
        mSrcImgPath = srcImgPath;
        mTargetImgFile = targetImgFile;
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;//仅解析,不分配内存,将返回null
        options.inSampleSize = 1;
        BitmapFactory.decodeFile(srcImgPath, options);
        mSrcWidth = options.outWidth;
        mSrcHeight = options.outHeight;
        mTargetMinSize = targetMinSize;
    }

    //计算长宽压缩比例值
    private int computeScale() {
        //计算最大边长与最小边长比,
        int longSide = Math.max(mSrcWidth, mSrcHeight);
        int shortSide = Math.min(mSrcWidth, mSrcHeight);
        return (int) Math.ceil((longSide / shortSide) >= (9 / 16) ? (longSide / 1280) : (shortSide / 1280));//9:16位黄金比例
    }

    ImageInfo compress() throws IOException {
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap targetBitmap = getBitmap(options);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        targetBitmap = ImageUtils.rotatingImage(targetBitmap, mSrcExif);
        int quality = 100;
        targetBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        while (outputStream.toByteArray().length > mTargetMinSize << 10) {
            outputStream.reset();
            quality -= 10;
            targetBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
        }
        FileOutputStream fos = new FileOutputStream(mTargetImgFile);
        fos.write(outputStream.toByteArray());
        fos.flush();
        fos.close();
        fos.close();
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setFile(mTargetImgFile).setWidth(options.outWidth).setHeight(options.outHeight);
        return imageInfo;
    }


    public Bitmap getBitmap(BitmapFactory.Options options) {
        options.inJustDecodeBounds = false;
        options.inSampleSize = computeScale();
        return BitmapFactory.decodeFile(mSrcImgPath, options);
    }

}
