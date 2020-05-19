package ydk.share;

/**
 * Created by Gsm on 2018/3/14.
 */

public class ShareModel {

    private String type;
    private String title;
    private String content;
    private String url;
    private String imgUrl;
    private int defImgResId;
    private String path;
    private int miniProgramType;

    public int getMiniProgramType() {
        return miniProgramType;
    }

    public void setMiniProgramType(int miniProgramType) {
        this.miniProgramType = miniProgramType;
    }

    public String getTitle() {
        return title;
    }

    public ShareModel setTitle(String title) {
        this.title = title;
        return this;
    }

    public String getContent() {
        return content;
    }

    public ShareModel setContent(String content) {
        this.content = content;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ShareModel setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public ShareModel setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
        return this;
    }

    public int getDefImgResId() {
        return defImgResId;
    }

    public ShareModel setDefImgResId(int defImgResId) {
        this.defImgResId = defImgResId;
        return this;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 'auto' | 'image' | 'audio' | 'video'
     */
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
