package ydk.album;

import com.luck.picture.lib.config.PictureConfig;

/**
 * Created by Gsm on 2018/5/4.
 */
public interface MediaPickConstants {
    int TYPE_PICTURE = PictureConfig.TYPE_IMAGE;
    int TYPE_VIDEO = PictureConfig.TYPE_VIDEO;
    int TYPE_AUDIO = PictureConfig.TYPE_AUDIO;
    int TYPE_ALL = PictureConfig.TYPE_ALL;//视频+图片

    int DEFAULT_NUM_COLUMNS = 4;//默认列
    boolean DEFAULT_SHOW_SHOOT = true;//默认显示拍摄

    boolean DEFAULT_PREVIEW = true;//默认预览图片&视频
    int DEFAULT_MAX_NUM = 9; //默认最大选择数
    boolean DEFAULT_CROP = false;//默认不裁剪
    boolean DEFAULT_CROP_CIRCLE = false;//默认不裁剪圆形
    int DEFAULT_CROP_SCALE = 1;//默认裁剪比例


}
