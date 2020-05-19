package ydk.mqtt.model.message;

/**
 * support topic规约
 * 约定仅工作在与EMQ的对接层，应避免侵入到业务逻辑中。
 * <p>
 * <p/>
 * Topic "tos/#"
 * 订阅者：server
 * 推送者：client
 * 1. 客户端发送个人消息tos/chat
 * 2. 客户端发送聊天室消息tos/chatRoom
 * <p>
 * <p/>
 * Topic "toc/#"
 * 订阅者：client
 * 推送者：server
 * 1. 服务端发送个人消息toc/#uid
 * 2. 服务端发送聊天室消息toc/chatRoom/#roomId
 * 3. 服务端发送全站广播消息toc/broadcast
 *
 * @author xiepeng
 * @version 1.0
 * @data 2019/6/5 0005 30
 */
public class TopicRule {

    /**
     * topic 一级目录约定
     */
    public static final String TOPIC_TO_SERVER_PREFIX = "tos/";

    public static final String TOPIC_TO_CLIENT_PREFIX = "toc/";

    /**
     * to server topic 目录约定
     *
     */
    public static final String TOPIC_TO_SERVER_CHATROOM = "tos/chatRoom";

    public static final String TOPIC_TO_SERVER_USER  = "tos/user";


    /**
     * to client topic 目录约定
     */
    public static final String TOPIC_TO_CLIENT_CHATROOM = "toc/chatRoom/";

    public static final String TOPIC_TO_CLIENT_USER = "toc/user/";

    public static final String TOPIC_TO_CLIENT_BROADCAST = "toc/broadcast";

}
