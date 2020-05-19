package ydk.mqtt.model.message.vo;

public class IMMessageVo {

    /**
     * 客户端消息id，使用uuid等随机串，msgId相同的消息会被客户端去重
     */
    private String msgId;

    /**
     * 消息发出者的账号uid
     */
    private Long fromUid;

    /**
     * 消息类型
     * 0: 文本消息
     * N: 自定义消息类型
     */
    private String msgType;

    /**
     * 消息正文
     */
    private String attach;

    /**
     * 消息扩展字段
     */
    private String ext;


    public String getMsgId() {
        return msgId;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }

    public Long getFromUid() {
        return fromUid;
    }

    public void setFromUid(Long fromUid) {
        this.fromUid = fromUid;
    }

    public String getMsgType() {
        return msgType;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public String getExt() {
        return ext;
    }

    public void setExt(String ext) {
        this.ext = ext;
    }
}
