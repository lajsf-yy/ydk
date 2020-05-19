package ydk.album;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.text.TextUtils;

import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.PictureSelectorActivity;
import com.luck.picture.lib.config.PictureConfig;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.entity.LocalMedia;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import ydk.core.activityresult.RxActivityResult;

/**
 * Created by Gsm on 2018/5/4.
 */
public class PicturePick {

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

    public Observable<HashMap<String,Object>> take(Activity currentActivity, int mimeType) {
        PictureSelector.create(currentActivity)
                .openCamera(mimeType);
//                .forResult(PictureConfig.CHOOSE_REQUEST);

        return RxActivityResult.on(currentActivity).startIntent(new Intent().setClass(currentActivity, PictureSelectorActivity.class))
                .map(result -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("type", MediaPickConstants.TYPE_PICTURE);
                    ArrayList<String> imageData = new ArrayList<>();
                    ArrayList<MediaInfo> videoData = new ArrayList<>();
                    if (result == null || result.data() == null) return map;
                    List<LocalMedia> list = PictureSelector.obtainMultipleResult(result.data());
                    if (list == null || list.isEmpty()) return map;

                    for (LocalMedia localMedia : list) {
                        if (localMedia == null) continue;
                        MediaInfo mediaInfo = new MediaInfo();
                        mediaInfo.setFilePath(TextUtils.isEmpty(localMedia.getCutPath()) ? localMedia.getPath() : localMedia.getCutPath());
                        mediaInfo.setDuration(localMedia.getDuration() / 1000);
                        mediaInfo.setSize(new File(mediaInfo.getFilePath()).length() / 1024);
                        if (PictureMimeType.isVideo(localMedia.getPictureType())) {
                            map.put("type", MediaPickConstants.TYPE_VIDEO);
                            mediaInfo.setThumbnailPath(localMedia.getThumbPath());
                            videoData.add(mediaInfo);
                        } else {
                            String imagePath = mediaInfo.getFilePath();
                            imageData.add(imagePath);
                        }
                    }
                    map.put("images", imageData);
                    map.put("videos", videoData);
                    return map;
                });
    }

    public Observable<HashMap<String, Object>> pick(Activity activity, PicturePickConfig config) {
        PictureSelectionModel model = PictureSelector.create(activity).openGallery(config.type)
                .theme(R.style.picture_style)
                .compress(false)
                .isGif(true)
                .scaleEnabled(true)
                .previewVideo(true)
                .recordVideoSecond(15)
                .videoMaxSecond(15)
                .videoMinSecond(1)
                .isZoomAnim(true)
                .rotateEnabled(false)
                .freeStyleCropEnabled(false)
                .hideBottomControls(true);
        if (config.style != null) {
            model.imageSpanCount(config.style.numColumns);
            model.isCamera(config.style.showCamera);
        }
        if ((config.type == MediaPickConstants.TYPE_ALL || config.type == MediaPickConstants.TYPE_PICTURE) && config.picture != null) {
            if (config.picture.isCrop) {
                model.selectionMode(PictureConfig.SINGLE);
                model.enableCrop(config.picture.isCrop);
                model.showCropGrid(!config.picture.isCropCircle);
                model.showCropFrame(!config.picture.isCropCircle);
                model.withAspectRatio((int) ((config.picture.cropScale == 0f ? 1 : config.picture.cropScale) * 100), 100);
            } else {
                model.selectionMode(PictureConfig.MULTIPLE);
                model.maxSelectNum(config.picture.maxNum == 0 ? MediaPickConstants.DEFAULT_MAX_NUM : config.picture.maxNum);
            }
        } else if (config.type == MediaPickConstants.TYPE_VIDEO) {
            if (config.style == null) {
                model.isCamera(false);
            }
            model.selectionMode(PictureConfig.SINGLE);
        }
        return RxActivityResult.on(activity).startIntent(new Intent().setClass(activity, PictureSelectorActivity.class))
                .map(result -> {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("type", MediaPickConstants.TYPE_PICTURE);
                    ArrayList<String> imageData = new ArrayList<>();
                    ArrayList<MediaInfo> videoData = new ArrayList<>();
                    if (result == null || result.data() == null) return map;
                    List<LocalMedia> list = PictureSelector.obtainMultipleResult(result.data());
                    if (list == null || list.isEmpty()) return map;

                    for (LocalMedia localMedia : list) {
                        if (localMedia == null) continue;
                        MediaInfo mediaInfo = new MediaInfo();
                        mediaInfo.setFilePath(TextUtils.isEmpty(localMedia.getCutPath()) ? localMedia.getPath() : localMedia.getCutPath());
                        mediaInfo.setDuration(localMedia.getDuration() / 1000);
                        mediaInfo.setSize(new File(mediaInfo.getFilePath()).length() / 1024);
                        if (PictureMimeType.isVideo(localMedia.getPictureType())) {
                            map.put("type", MediaPickConstants.TYPE_VIDEO);
//                            Bitmap thumbnail = videoCmd.getVideoThumbnail(mediaInfo.getFilePath());
//                            Bitmap thumbnail = getVideoThumbnail(localMedia.getPath());
//                            if (thumbnail != null) {
//                                mediaInfo.setThumbnailPath(ImageUtils.saveTo(thumbnail, "video"));
//                                if (!thumbnail.isRecycled()) thumbnail.recycle();
//                            }
                            mediaInfo.setThumbnailPath(localMedia.getThumbPath());
                            videoData.add(mediaInfo);
                        } else {
                            String imagePath = mediaInfo.getFilePath();
                            imageData.add(imagePath);
                        }
                    }
                    map.put("images", imageData);
                    map.put("videos", videoData);
                    return map;
                });
    }

}
