package ydk.mqtt.model.message.vo;

/**
 * 聊天室消息返回格式
 */
public class ChatRoomMessageVo extends IMMessageVo {

    /**
     * 聊天室id
     */
    private String roomId;

    /**
     * 客户端消息id，使用uuid等随机串，msgId相同的消息会被客户端去重
     */

    /**
     * 消息发出者昵称
     */
    private String fromUname;


    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getFromUname() {
        return fromUname;
    }

    public void setFromUname(String fromUname) {
        this.fromUname = fromUname;
    }
}
