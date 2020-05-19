package ydk.album;

/**
 * Created by Gsm on 2018/5/4.
 */
public class MediaInfo {
    private String filePath;
    private String thumbnailPath;
    private long size;//kb
    private long duration;//second

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getThumbnailPath() {
        return thumbnailPath;
    }

    public void setThumbnailPath(String thumbnailPath) {
        this.thumbnailPath = thumbnailPath;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("filePath=").append(filePath)
                .append(",thumbnailPath=").append(thumbnailPath)
                .append(",size=").append(size)
                .append(",duration=").append(duration);
        return stringBuilder.toString();
    }
}
