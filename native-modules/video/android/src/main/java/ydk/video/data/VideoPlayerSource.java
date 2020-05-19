package ydk.video.data;

public class VideoPlayerSource {
    /**
     *  视频播放链接地址
     */
    private String uri;
    /**
     * aspect | aspectFill | resize
     */
    private String videoGravity;

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getVideoGravity() {
        return videoGravity;
    }

    public void setVideoGravity(String videoGravity) {
        this.videoGravity = videoGravity;
    }
}
