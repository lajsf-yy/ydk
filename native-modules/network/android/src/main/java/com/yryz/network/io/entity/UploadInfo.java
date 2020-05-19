package com.yryz.network.io.entity;

/**
 * Created by Administrator on 2018/2/27.
 */

public class UploadInfo {
    private int width;
    private int height;
    private String localFile;
    private String url;
    private long total;
    private long uploadBytes;
    private boolean isCompleted = false;

    public String getLocalFile() {
        return localFile;
    }

    public void setLocalFile(String uploadFilePath) {
        this.localFile = uploadFilePath;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getUploadBytes() {
        return uploadBytes;
    }

    public void setUploadBytes(long uploadBytes) {
        this.uploadBytes = uploadBytes;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }
}
