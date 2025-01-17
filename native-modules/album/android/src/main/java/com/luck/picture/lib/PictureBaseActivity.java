package com.luck.picture.lib;

import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.core.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.luck.picture.lib.compress.Luban;
import com.luck.picture.lib.compress.OnCompressListener;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.dialog.PictureDialog;
import com.luck.picture.lib.entity.EventEntity;
import com.luck.picture.lib.entity.LocalMedia;
import com.luck.picture.lib.entity.LocalMediaFolder;
import com.luck.picture.lib.immersive.ImmersiveManage;
import com.luck.picture.lib.rxbus2.RxBus;
import com.luck.picture.lib.tools.AttrsUtils;
import com.luck.picture.lib.tools.DateUtils;
import com.luck.picture.lib.tools.DoubleUtils;
import com.luck.picture.lib.tools.PictureFileUtils;
import com.luck.picture.lib.tools.ToastManage;
import com.luck.picture.transcoder.MediaTranscoder;
import com.luck.picture.transcoder.format.MediaFormatStrategyPresets;
import com.yalantis.ucrop.UCrop;
import com.yalantis.ucrop.UCropMulti;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;
import ydk.album.R;

/**
 * @author：luck
 * @data：2018/3/28 下午1:00
 * @描述: Activity基类
 */
public class PictureBaseActivity extends FragmentActivity {
    protected Context mContext;
    protected PictureSelectionConfig config;
    protected boolean openWhiteStatusBar, numComplete;
    protected int colorPrimary, colorPrimaryDark;
    protected String cameraPath, outputCameraPath;
    protected String originalPath;
    protected PictureDialog dialog;
    protected PictureDialog compressDialog;
    protected List<LocalMedia> selectionMedias;

    /**
     * 是否使用沉浸式，子类复写该方法来确定是否采用沉浸式
     *
     * @return 是否沉浸式，默认true
     */
    @Override
    public boolean isImmersive() {
        return true;
    }

    /**
     * 具体沉浸的样式，可以根据需要自行修改状态栏和导航栏的颜色
     */
    public void immersive() {
        ImmersiveManage.immersiveAboveAPI23(this
                , colorPrimaryDark
                , colorPrimary
                , openWhiteStatusBar);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            config = savedInstanceState.getParcelable(PictureConfig.EXTRA_CONFIG);
            cameraPath = savedInstanceState.getString(PictureConfig.BUNDLE_CAMERA_PATH);
            originalPath = savedInstanceState.getString(PictureConfig.BUNDLE_ORIGINAL_PATH);
        } else {
            config = PictureSelectionConfig.getInstance();
        }
        int themeStyleId = config.themeStyleId;
        setTheme(themeStyleId);
        super.onCreate(savedInstanceState);
        mContext = this;
        initConfig();
        if (isImmersive()) {
            immersive();
        }
    }

    /**
     * 获取配置参数
     */
    private void initConfig() {
        outputCameraPath = config.outputCameraPath;
        // 是否开启白色状态栏
        openWhiteStatusBar = AttrsUtils.getTypeValueBoolean
                (this, R.attr.picture_statusFontColor);
        // 是否是0/9样式
        numComplete = AttrsUtils.getTypeValueBoolean(this,
                R.attr.picture_style_numComplete);
        // 是否开启数字勾选模式
        config.checkNumMode = AttrsUtils.getTypeValueBoolean
                (this, R.attr.picture_style_checkNumMode);
        // 标题栏背景色
        colorPrimary = AttrsUtils.getTypeValueColor(this, R.attr.colorPrimary);
        // 状态栏背景色
        colorPrimaryDark = AttrsUtils.getTypeValueColor(this, R.attr.colorPrimaryDark);
        // 已选图片列表
        selectionMedias = config.selectionMedias;
        if (selectionMedias == null) {
            selectionMedias = new ArrayList<>();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(PictureConfig.BUNDLE_CAMERA_PATH, cameraPath);
        outState.putString(PictureConfig.BUNDLE_ORIGINAL_PATH, originalPath);
        outState.putParcelable(PictureConfig.EXTRA_CONFIG, config);
    }

    protected void startActivity(Class clz, Bundle bundle) {
        if (!DoubleUtils.isFastDoubleClick()) {
            Intent intent = new Intent();
            intent.setClass(this, clz);
            intent.putExtras(bundle);
            startActivity(intent);
        }
    }

    protected void startActivity(Class clz, Bundle bundle, int requestCode) {
        if (!DoubleUtils.isFastDoubleClick()) {
            Intent intent = new Intent();
            intent.setClass(this, clz);
            intent.putExtras(bundle);
            startActivityForResult(intent, requestCode);
        }
    }

    /**
     * loading dialog
     */
    protected void showPleaseDialog() {
        if (!isFinishing()) {
            dismissDialog();
            dialog = new PictureDialog(this);
            dialog.show();
        }
    }

    /**
     * dismiss dialog
     */
    protected void dismissDialog() {
        try {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * compress loading dialog
     */
    protected void showCompressDialog() {
        if (!isFinishing()) {
            dismissCompressDialog();
            compressDialog = new PictureDialog(this);
            compressDialog.show();
        }
    }

    protected void showCompressDialog(DialogInterface.OnCancelListener listener) {
        if (!isFinishing()) {
            dismissCompressDialog();
            compressDialog = new PictureDialog(this);
            compressDialog.setOnCancelListener(listener);
            compressDialog.show();
        }
    }

    /**
     * dismiss compress dialog
     */
    protected void dismissCompressDialog() {
        try {
            if (!isFinishing()
                    && compressDialog != null
                    && compressDialog.isShowing()) {
                compressDialog.dismiss();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getPath(String dirName) {
        File cacheDir = getExternalCacheDir();
        if (cacheDir != null) {
            File result = new File(cacheDir, dirName);
            if (!result.mkdirs() && (!result.exists() || !result.isDirectory())) {
                return null;
            }
            return result.getAbsolutePath();
        }
        return "";
    }

    public Bitmap getVideoThumbnail(String filePath) {
        Bitmap bitmap;
        MediaMetadataRetriever retriever = null;
        try {
            retriever = new MediaMetadataRetriever();
            retriever.setDataSource(filePath);
//            bitmap = retriever.getFrameAtTime();
            bitmap = retriever.getFrameAtTime(0);
        } catch (Exception e) {
            bitmap = null;
        } finally {
            if (retriever != null) retriever.release();
        }
        return bitmap;
    }

    public String saveTo(Bitmap bm, String dirPath) {
        if (bm == null) return "";
        dirPath = TextUtils.isEmpty(dirPath) ? String.valueOf(System.currentTimeMillis()) : dirPath;
        //新建文件夹用于存放裁剪后的图片
        File tmpDir = new File(new File(getExternalCacheDir(), "thumb"), dirPath);
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }
        String uuid = UUID.randomUUID().toString();
        //新建文件存储裁剪后的图片
        try {
            String localFile = tmpDir.getAbsolutePath() + "/" + uuid + ".jpg";
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

    Disposable disposable = null;
    Future<Void> mFuture = null;
    volatile boolean isCanceled = false;

    protected void compressVideo(final List<LocalMedia> result) {

        final LocalMedia localMedia = result.get(0);

//        if (localMedia.getDuration() <= 60*1000) {
//            onResult(result);
//            return;
//        }
        final int maxDuration = 5*60*1000;
//        Log.i("xxx", "video duration is " + localMedia.getDuration());
        if (localMedia.getDuration() - maxDuration > 0) {
            ToastManage.s(mContext, "视频最多支持5分钟时长，请重新选择");
            return;
        }

        String filepath = TextUtils.isEmpty(localMedia.getCutPath()) ? localMedia.getPath() : localMedia.getCutPath();

        File target = new File(filepath);
        if (!target.exists()) {
            ToastManage.s(mContext, "找不到该文件");
            return;
        }

        if (target.length() > 162144000) {//250M
            ToastManage.s(mContext, "视频仅支持250M以内大小，请重新选择");
            return;
        }


        if (!checkCompressNecessary(localMedia.getDuration(), target)) {
//            Log.i("xxx", "compress unnecessary");
            showCompressDialog();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    List<LocalMedia> media = new ArrayList<>();

                    Bitmap thumbnail = getVideoThumbnail(localMedia.getPath());
                    if (thumbnail != null) {
                        localMedia.setThumbPath(saveTo(thumbnail, "video"));
                        if (!thumbnail.isRecycled()) thumbnail.recycle();
                    }
                    media.add(localMedia);

                    RxBus.getDefault().post(new EventEntity(PictureConfig.CLOSE_PREVIEW_FLAG));
                    onResult(media);
                }
            }).start();
            return;
        }

        isCanceled = false;
        showCompressDialog(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                if (null != mFuture) {
                    try {
                        if (!mFuture.isDone()) {
                            mFuture.cancel(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                isCanceled = true;

                Toast.makeText(mContext.getApplicationContext(), "已取消", Toast.LENGTH_SHORT)
                        .show();
            }
        });

//        Log.i("xxx", "origin filepath is " + filepath);
//        Log.i("xxx", "origin filesize is " + new File(filepath).length() / 1024);
        File tmpDir = new File(getPath("video"), "compress");
        String uuid = UUID.randomUUID().toString();
        final String targetFilePath = tmpDir.getAbsolutePath() + "/" + uuid + ".mp4";
        File dir = new File(targetFilePath).getParentFile();
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
//        Log.i("xxx", "create new file: " + targetFilePath);

        final PublishSubject<Integer> publishSubject = PublishSubject.create();

        ContentResolver resolver = getContentResolver();
        final ParcelFileDescriptor parcelFileDescriptor;
        Uri uri = FileProvider.getUriForFile(mContext, getApplication().getPackageName()+".provider", new File(filepath));
        try {
            parcelFileDescriptor = resolver.openFileDescriptor(uri, "r");
        } catch (FileNotFoundException e) {
            Log.w("Could not open '" + uri.toString() + "'", e);
            Toast.makeText(this, "File not found.", Toast.LENGTH_LONG).show();
            return;
        }
        final FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
//                    final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progress_bar);
//                    progressBar.setMax(PROGRESS_BAR_MAX);
//        final long startTime = SystemClock.uptimeMillis();
        MediaTranscoder.Listener listener = new MediaTranscoder.Listener() {
            @Override
            public void onTranscodeProgress(double progress) {
//                Log.i("xxx", "progress with " + progress);
                int percent = (int) (progress*100);
                publishSubject.onNext(percent);
            }

            @Override
            public void onTranscodeCompleted() {
                if (!disposable.isDisposed()) {
                    disposable.dispose();
                }

                if (isCanceled) {
                    return;
                }
//                LocalMedia localMedia = result.get(0);

                List<LocalMedia> media = new ArrayList<>();
//                localMedia.setPath(targetFilePath);
                localMedia.setCutPath(targetFilePath);
                Bitmap thumbnail = getVideoThumbnail(localMedia.getPath());
                if (thumbnail != null) {
                    localMedia.setThumbPath(saveTo(thumbnail, "video"));
                    if (!thumbnail.isRecycled()) thumbnail.recycle();
                }
                media.add(localMedia);

                RxBus.getDefault().post(new EventEntity(PictureConfig.CLOSE_PREVIEW_FLAG));
                onResult(media);
            }

            @Override
            public void onTranscodeCanceled() {
                if (!disposable.isDisposed()) {
                    disposable.dispose();
                }
            }

            @Override
            public void onTranscodeFailed(Exception exception) {
                if (!disposable.isDisposed()) {
                    disposable.dispose();
                }
            }

            @Override
            public boolean isTranscodeCanceled() {
                return isCanceled;
            }
        };
        mFuture = MediaTranscoder.getInstance().transcodeVideo(fileDescriptor, targetFilePath,
                MediaFormatStrategyPresets.createAndroid720pStrategy(), listener);
//        VideoCompress.compressVideoLow(filepath, targetFilePath, new VideoCompress.CompressListener() {
//            @Override
//            public void onStart() {
////                Log.i("xxx", "start compress");
//                Toast.makeText(mContext.getApplicationContext(), "正在转码，请稍后", Toast.LENGTH_SHORT)
//                        .show();
//            }
//
//            @Override
//            public void onSuccess() {
////                Log.i("xxx", "finish compress");
//
//                if (!disposable.isDisposed()) {
//                    disposable.dispose();
//                }
//
//                RxBus.getDefault().post(new EventEntity(PictureConfig.CLOSE_PREVIEW_FLAG));
//
////                LocalMedia localMedia = result.get(0);
//
//                List<LocalMedia> media = new ArrayList<>();
//                localMedia.setPath(targetFilePath);
//                localMedia.setCutPath(targetFilePath);
//                media.add(localMedia);
//                onResult(media);
//            }
//
//            @Override
//            public void onFail() {
//                if (!disposable.isDisposed()) {
//                    disposable.dispose();
//                }
////                Log.i("xxx", "failed compress");
//                Toast.makeText(mContext.getApplicationContext(), "转码失败", Toast.LENGTH_SHORT)
//                        .show();
//            }
//
//            @Override
//            public void onProgress(float percent) {
//                publishSubject.onNext((int) percent);
//            }
//        });

        disposable = publishSubject.throttleLast(2, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        Toast.makeText(mContext.getApplicationContext(), integer+"%", Toast.LENGTH_SHORT)
                                .show();
                    }
                });

    }

    private boolean checkCompressNecessary(long duration, File target) {
        int minute = (int) (duration/1000/60);
        if (minute <= 0) {
            int seconds = (int) (duration/1000);
            if (seconds <= 15) {
                return target.length()/1024/1024 - 8 > 0;//more than 8M
            }
        }
//        Log.i("xxx", "file size is " + target.length()/1024/1024 + "M");
        return target.length()/1024/1024 - (minute+1)*30 > 0;// per 1m more than 30M
    }

    /**
     * compressImage
     */
    protected void compressImage(final List<LocalMedia> result) {
        showCompressDialog();
        if (config.synOrAsy) {
            Flowable.just(result)
                    .observeOn(Schedulers.io())
                    .map(new Function<List<LocalMedia>, List<File>>() {
                        @Override
                        public List<File> apply(@NonNull List<LocalMedia> list) throws Exception {
                            List<File> files = Luban.with(mContext)
                                    .setTargetDir(config.compressSavePath)
                                    .ignoreBy(config.minimumCompressSize)
                                    .loadLocalMedia(list).get();
                            if (files == null) {
                                files = new ArrayList<>();
                            }
                            return files;
                        }
                    })
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<List<File>>() {
                        @Override
                        public void accept(@NonNull List<File> files) throws Exception {
                            handleCompressCallBack(result, files);
                        }
                    });
        } else {
            Luban.with(this)
                    .loadLocalMedia(result)
                    .ignoreBy(config.minimumCompressSize)
                    .setTargetDir(config.compressSavePath)
                    .setCompressListener(new OnCompressListener() {
                        @Override
                        public void onStart() {
                        }

                        @Override
                        public void onSuccess(List<LocalMedia> list) {
                            RxBus.getDefault().post(new EventEntity(PictureConfig.CLOSE_PREVIEW_FLAG));
                            onResult(list);
                        }

                        @Override
                        public void onError(Throwable e) {
                            RxBus.getDefault().post(new EventEntity(PictureConfig.CLOSE_PREVIEW_FLAG));
                            onResult(result);
                        }
                    }).launch();
        }
    }

    /**
     * 重新构造已压缩的图片返回集合
     *
     * @param images
     * @param files
     */
    private void handleCompressCallBack(List<LocalMedia> images, List<File> files) {
        if (files.size() == images.size()) {
            for (int i = 0, j = images.size(); i < j; i++) {
                // 压缩成功后的地址
                String path = files.get(i).getPath();
                LocalMedia image = images.get(i);
                // 如果是网络图片则不压缩
                boolean http = PictureMimeType.isHttp(path);
                boolean eqTrue = !TextUtils.isEmpty(path) && http;
                image.setCompressed(eqTrue ? false : true);
                image.setCompressPath(eqTrue ? "" : path);
            }
        }
        RxBus.getDefault().post(new EventEntity(PictureConfig.CLOSE_PREVIEW_FLAG));
        onResult(images);
    }

    /**
     * 去裁剪
     *
     * @param originalPath
     */
    protected void startCrop(String originalPath) {
        UCrop.Options options = new UCrop.Options();
        int toolbarColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_crop_toolbar_bg);
        int statusColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_crop_status_color);
        int titleColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_crop_title_color);
        options.setToolbarColor(toolbarColor);
        options.setStatusBarColor(statusColor);
        options.setToolbarWidgetColor(titleColor);
        options.setCircleDimmedLayer(config.circleDimmedLayer);
        options.setShowCropFrame(config.showCropFrame);
        options.setShowCropGrid(config.showCropGrid);
        options.setDragFrameEnabled(config.isDragFrame);
        options.setScaleEnabled(config.scaleEnabled);
        options.setRotateEnabled(config.rotateEnabled);
        options.setCompressionQuality(config.cropCompressQuality);
        options.setHideBottomControls(config.hideBottomControls);
        options.setFreeStyleCropEnabled(config.freeStyleCropEnabled);
        boolean isHttp = PictureMimeType.isHttp(originalPath);
        String imgType = PictureMimeType.getLastImgType(originalPath);
        Uri uri = isHttp ? Uri.parse(originalPath) : Uri.fromFile(new File(originalPath));
        UCrop.of(uri, Uri.fromFile(new File(PictureFileUtils.getDiskCacheDir(this),
                System.currentTimeMillis() + imgType)))
                .withAspectRatio(config.aspect_ratio_x, config.aspect_ratio_y)
                .withMaxResultSize(config.cropWidth, config.cropHeight)
                .withOptions(options)
                .start(this);
    }

    /**
     * 多图去裁剪
     *
     * @param list
     */
    protected void startCrop(ArrayList<String> list) {
        UCropMulti.Options options = new UCropMulti.Options();
        int toolbarColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_crop_toolbar_bg);
        int statusColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_crop_status_color);
        int titleColor = AttrsUtils.getTypeValueColor(this, R.attr.picture_crop_title_color);
        options.setToolbarColor(toolbarColor);
        options.setStatusBarColor(statusColor);
        options.setToolbarWidgetColor(titleColor);
        options.setCircleDimmedLayer(config.circleDimmedLayer);
        options.setShowCropFrame(config.showCropFrame);
        options.setDragFrameEnabled(config.isDragFrame);
        options.setShowCropGrid(config.showCropGrid);
        options.setScaleEnabled(config.scaleEnabled);
        options.setRotateEnabled(config.rotateEnabled);
        options.setHideBottomControls(true);
        options.setCompressionQuality(config.cropCompressQuality);
        options.setCutListData(list);
        options.setFreeStyleCropEnabled(config.freeStyleCropEnabled);
        String path = list.size() > 0 ? list.get(0) : "";
        boolean isHttp = PictureMimeType.isHttp(path);
        String imgType = PictureMimeType.getLastImgType(path);
        Uri uri = isHttp ? Uri.parse(path) : Uri.fromFile(new File(path));
        UCropMulti.of(uri, Uri.fromFile(new File(PictureFileUtils.getDiskCacheDir(this),
                System.currentTimeMillis() + imgType)))
                .withAspectRatio(config.aspect_ratio_x, config.aspect_ratio_y)
                .withMaxResultSize(config.cropWidth, config.cropHeight)
                .withOptions(options)
                .start(this);
    }


    /**
     * 判断拍照 图片是否旋转
     *
     * @param degree
     * @param file
     */
    protected void rotateImage(int degree, File file) {
        if (degree > 0) {
            // 针对相片有旋转问题的处理方式
            try {
                BitmapFactory.Options opts = new BitmapFactory.Options();//获取缩略图显示到屏幕上
                opts.inSampleSize = 2;
                Bitmap bitmap = BitmapFactory.decodeFile(file.getAbsolutePath(), opts);
                Bitmap bmp = PictureFileUtils.rotaingImageView(degree, bitmap);
                PictureFileUtils.saveBitmapFile(bmp, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * compress or callback
     *
     * @param result
     */
    protected void handlerResult(List<LocalMedia> result) {
        if (config.isCompress) {
            compressImage(result);
        } else {
            onResult(result);
        }
    }


    /**
     * 如果没有任何相册，先创建一个最近相册出来
     *
     * @param folders
     */
    protected void createNewFolder(List<LocalMediaFolder> folders) {
        if (folders.size() == 0) {
            // 没有相册 先创建一个最近相册出来
            LocalMediaFolder newFolder = new LocalMediaFolder();
            String folderName = config.mimeType == PictureMimeType.ofAudio() ?
                    getString(R.string.yui_picture_all_audio) : getString(R.string.yui_picture_camera_roll);
            newFolder.setName(folderName);
            newFolder.setPath("");
            newFolder.setFirstImagePath("");
            folders.add(newFolder);
        }
    }

    /**
     * 将图片插入到相机文件夹中
     *
     * @param path
     * @param imageFolders
     * @return
     */
    protected LocalMediaFolder getImageFolder(String path, List<LocalMediaFolder> imageFolders) {
        File imageFile = new File(path);
        File folderFile = imageFile.getParentFile();

        for (LocalMediaFolder folder : imageFolders) {
            if (folder.getName().equals(folderFile.getName())) {
                return folder;
            }
        }
        LocalMediaFolder newFolder = new LocalMediaFolder();
        newFolder.setName(folderFile.getName());
        newFolder.setPath(folderFile.getAbsolutePath());
        newFolder.setFirstImagePath(path);
        imageFolders.add(newFolder);
        return newFolder;
    }

    /**
     * return image result
     *
     * @param images
     */
    protected void onResult(List<LocalMedia> images) {
        dismissCompressDialog();
        if (config.camera
                && config.selectionMode == PictureConfig.MULTIPLE
                && selectionMedias != null) {
            images.addAll(images.size() > 0 ? images.size() - 1 : 0, selectionMedias);
        }
        Intent intent = PictureSelector.putIntentResult(images);
        setResult(RESULT_OK, intent);
        closeActivity();
    }

    /**
     * Close Activity
     */
    protected void closeActivity() {
        finish();
        if (config.camera) {
            overridePendingTransition(0, R.anim.yui_fade_out);
        } else {
            overridePendingTransition(0, R.anim.yui_a3);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (null != mFuture) {
            try {
                if (!mFuture.isDone()) {
                    mFuture.cancel(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (null != disposable) {
            if (!disposable.isDisposed()) {
                disposable.dispose();
            }
        }
        dismissCompressDialog();
        dismissDialog();
    }


    /**
     * 获取DCIM文件下最新一条拍照记录
     *
     * @return
     */
    protected int getLastImageId(boolean eqVideo) {
        try {
            //selection: 指定查询条件
            String absolutePath = PictureFileUtils.getDCIMCameraPath();
            String ORDER_BY = MediaStore.Files.FileColumns._ID + " DESC";
            String selection = eqVideo ? MediaStore.Video.Media.DATA + " like ?" :
                    MediaStore.Images.Media.DATA + " like ?";
            //定义selectionArgs：
            String[] selectionArgs = {absolutePath + "%"};
            Cursor imageCursor = this.getContentResolver().query(eqVideo ?
                            MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                            : MediaStore.Images.Media.EXTERNAL_CONTENT_URI, null,
                    selection, selectionArgs, ORDER_BY);
            if (imageCursor.moveToFirst()) {
                int id = imageCursor.getInt(eqVideo ?
                        imageCursor.getColumnIndex(MediaStore.Video.Media._ID)
                        : imageCursor.getColumnIndex(MediaStore.Images.Media._ID));
                long date = imageCursor.getLong(eqVideo ?
                        imageCursor.getColumnIndex(MediaStore.Video.Media.DURATION)
                        : imageCursor.getColumnIndex(MediaStore.Images.Media.DATE_ADDED));
                int duration = DateUtils.dateDiffer(date);
                imageCursor.close();
                // DCIM文件下最近时间30s以内的图片，可以判定是最新生成的重复照片
                return duration <= 30 ? id : -1;
            } else {
                return -1;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 删除部分手机 拍照在DCIM也生成一张的问题
     *
     * @param id
     * @param eqVideo
     */
    protected void removeImage(int id, boolean eqVideo) {
        try {
            ContentResolver cr = getContentResolver();
            Uri uri = eqVideo ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                    : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
            String selection = eqVideo ? MediaStore.Video.Media._ID + "=?"
                    : MediaStore.Images.Media._ID + "=?";
            cr.delete(uri,
                    selection,
                    new String[]{Long.toString(id)});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 录音
     *
     * @param data
     */
    protected String getAudioPath(Intent data) {
        boolean compare_SDK_19 = Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT;
        if (data != null && config.mimeType == PictureMimeType.ofAudio()) {
            try {
                Uri uri = data.getData();
                final String audioPath;
                if (compare_SDK_19) {
                    audioPath = uri.getPath();
                } else {
                    audioPath = getAudioFilePathFromUri(uri);
                }
                return audioPath;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return "";
    }

    /**
     * 获取刚录取的音频文件
     *
     * @param uri
     * @return
     */
    protected String getAudioFilePathFromUri(Uri uri) {
        String path = "";
        try {
            Cursor cursor = getContentResolver()
                    .query(uri, null, null, null, null);
            cursor.moveToFirst();
            int index = cursor.getColumnIndex(MediaStore.Audio.AudioColumns.DATA);
            path = cursor.getString(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return path;
    }
}
