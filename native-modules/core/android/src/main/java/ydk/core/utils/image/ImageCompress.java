package ydk.core.utils.image;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import ydk.core.utils.FileUtils;

/**
 * Created by Gsm on 2018/3/21.
 */

public class ImageCompress {

    private Builder mBuilder;

    private ImageCompress(Builder builder) {
        mBuilder = builder;
    }

    public static Builder with(Context context) {
        return new Builder(context);
    }

    public Observable<List<ImageInfo>> compress() {
        return Observable.create((ObservableOnSubscribe) emitter -> compress(emitter))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    private void compress(ObservableEmitter emitter) {
        ArrayList<ImageInfo> resultList = new ArrayList<>();
        if (mBuilder.mImgPaths != null && !mBuilder.mImgPaths.isEmpty()) {
            Iterator<String> iterator = mBuilder.mImgPaths.iterator();
            while (iterator.hasNext()) {
                String imgPath = iterator.next();
                compressCore(imgPath, resultList, emitter);
                iterator.remove();
            }
        }
        if (mBuilder.mImgUri != null) {
            String path = getPath(mBuilder.mImgUri, mBuilder.mContext);
            compressCore(path, resultList, emitter);
        }
        emitter.onNext(resultList);
        emitter.onComplete();
    }

    private void compressCore(String imgPath, ArrayList<ImageInfo> resultList, ObservableEmitter emitter) {
        if (Checker.isImage(imgPath)) {
            try {
                resultList.add(new CompressCore(imgPath, getImageCacheFile(mBuilder.mContext, FileUtils.getSuffix(imgPath)), mBuilder.mMinSize).compress());
            } catch (IOException e) {
                emitter.onError(e);
            }
        }
    }

    private String getPath(Uri uri, Context context) {
        String[] types = {MediaStore.MediaColumns.DATA};
        Cursor cursor = context.getContentResolver().query(uri, types, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(types[0]);
        String path = cursor.getString(columnIndex);
        cursor.close();
        return path;
    }

    private File getImageCacheFile(Context context, String suffix) {
        String targetDir = getImageCacheDir(context, context.getApplicationContext().getPackageName()).getAbsolutePath();
        String cacheBuilder = targetDir + "/" + System.currentTimeMillis() + (TextUtils.isEmpty(suffix) ? ".jpg" : suffix);
        return new File(cacheBuilder);
    }

    private File getImageCacheDir(Context context, String cacheName) {
        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, cacheName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                return null;
            }
            return result;
        }
        return null;
    }

    public static class Builder {
        private Context mContext;
        private List<String> mImgPaths;
        private Uri mImgUri;
        private int mMinSize = 300;

        Builder(Context context) {
            mContext = context;
            mImgPaths = new ArrayList<>();
        }

        /**
         * <= minSize的图片文件大小将不做压缩 ; 且图片压缩最小至minSize
         *
         * @param size 单位kb,默认值300
         * @return
         */
        public Builder setMinSize(int size) {
            mMinSize = size;
            return this;
        }

        public Builder load(Uri imgUri) {
            mImgUri = imgUri;
            return this;
        }

        public Builder load(List<String> list) {
            mImgPaths.addAll(list);
            return this;
        }

        public Observable<List<ImageInfo>> compress() {
            return build().compress();
        }

        private ImageCompress build() {
            return new ImageCompress(this);
        }
    }


}
