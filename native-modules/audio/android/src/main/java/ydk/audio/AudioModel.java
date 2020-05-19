package ydk.audio;

public class AudioModel {

    /**
     * 操作ID
     */
    private double tagId;

    /**
     * 音频总时长
     */
    private double duration;
    /**
     * 音频的播放进度
     */
    private double progress;
    /**
     * 音频的缓冲进度
     */
    private double playableDuration;

    /**
     * 错误码
     */
    private int code;


    public AudioModel(double tagId) {
        this.tagId = tagId;
    }

    public double getTagId() {
        return tagId;
    }


    public double getDuration() {
        return duration;
    }

    public void setDuration(double duration) {
        this.duration = duration;
    }

    public double getProgress() {
        return progress;
    }

    public void setProgress(double progress) {
        this.progress = progress;
    }

    public double getPlayableDuration() {
        return playableDuration;
    }

    public void setPlayableDuration(double playableDuration) {
        this.playableDuration = playableDuration;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String toString() {
        return "AudioModel{" +
                ", tagId=" + tagId +
                ", duration=" + duration +
                ", progress=" + progress +
                ", playableDuration=" + playableDuration +
                '}';
    }


}
