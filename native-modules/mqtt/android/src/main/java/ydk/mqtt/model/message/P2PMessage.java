package ydk.mqtt.model.message;

/**
 * 单聊消息返回格式
 */
public class P2PMessage extends IMMessage {

    /**
     * 消息接收者的账号uid
     */
    private Long toUid;

    public Long getToUid() {
        return toUid;
    }

    public void setToUid(Long toUid) {
        this.toUid = toUid;
    }
}
