package ydk.album;

/**
 * Created by Gsm on 2018/5/4.
 */
public class PicturePickConfig {

    public int type = MediaPickConstants.TYPE_PICTURE;//选择类型
    public Style style = new Style();
    public Picture picture = new Picture();

    public class Style {
        public int numColumns = MediaPickConstants.DEFAULT_NUM_COLUMNS;//列数
        public boolean showCamera = MediaPickConstants.DEFAULT_SHOW_SHOOT;//是否显示拍摄
    }

    public class Picture {
        public int maxNum = MediaPickConstants.DEFAULT_MAX_NUM;//最大选择数
        public boolean isCrop = MediaPickConstants.DEFAULT_CROP;//是否裁剪 , 当isCrop为true时不考虑maxNum,此时maxNum默认为1;
        public boolean isCropCircle = MediaPickConstants.DEFAULT_CROP_CIRCLE;//是否裁剪圆形,当isCrop为true时生效
        public float cropScale = 1;//默认裁剪比例,当isCropCircle为false时生效,scale = w:h;


    }

}
