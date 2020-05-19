package com.yryz.network.io.entity;

public class DownloadInfo {
    public static final int TOTAL_ERROR = -1;//获取进度失败
    private String url;
    private int progress = TOTAL_ERROR;
    private long total;
    private long downloadBytes;
    private String fileName;
    private String filePath;
    private boolean isCompleted = false;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public DownloadInfo(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getDownloadBytes() {
        return downloadBytes;
    }

    public void setDownloadBytes(long downloadBytes) {
        this.downloadBytes = downloadBytes;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    public int getProgress() {
        return progress;
    }

    public void setProgress(int progress) {
        this.progress = progress;
    }
}
