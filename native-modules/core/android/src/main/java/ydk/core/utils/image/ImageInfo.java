package ydk.core.utils.image;

import java.io.File;

/**
 * Created by Gsm on 2018/4/2.
 */

public class ImageInfo {
    /**
     * 本地路径
     */
    private File file;
    /**
     * url地址
     */
    private String imgUrl;
    /**
     * 宽
     */
    private int width;
    /**
     * 高
     */
    private int height;

    public File getFile() {
        return file;
    }

    public ImageInfo setFile(File file) {
        this.file = file;
        return this;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public ImageInfo setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
        return this;
    }

    public int getWidth() {
        return width;
    }

    public ImageInfo setWidth(int width) {
        this.width = width;
        return this;
    }

    public int getHeight() {
        return height;
    }

    public ImageInfo setHeight(int height) {
        this.height = height;
        return this;
    }
}
