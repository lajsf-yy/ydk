package ydk.mqtt.engine.service

import ydk.mqtt.RequestCallback
import ydk.mqtt.SimpleRequestCallback
import ydk.mqtt.model.message.P2PMessage
import ydk.mqtt.model.message.attachment.ExtAttachment
import ydk.mqtt.model.message.parser.MsgExtAttachmentParser
import ydk.mqtt.proxy.ServerAnnotation


@ServerAnnotation(clazz = MessageServiceImpl::class)
interface MessageService : IMServer {
    /**
     * 注册自定义消息解析器
     * @param msgExtAttachmentParser
     */
    fun registerMsgExtAttachmentParser(msgExtAttachmentParser: MsgExtAttachmentParser, register: Boolean = true)

    /**
     * 序列化自定义消息的扩展类型
     *
     * @param msgType 消息的type
     * @param attach 扩展字段的 json 字符串
     */
    fun serializeMsgExtAttachment(msgType: String, attach: String): ExtAttachment?

    /**
     * 发送P2PMessage消息 运用的MQTT发送
     * @param msg 消息
     * @param callback 状态回调者
     */
    fun sendP2PMessage(msg: P2PMessage, callback: RequestCallback<Void> = SimpleRequestCallback())

    /**
     * 发送P2PMessage消息 运用的HTTP发送
     * @param msg 消息
     * @param callback 状态回调者
     */
    fun sendP2PMessage2(msg: P2PMessage, callback: RequestCallback<Void> = SimpleRequestCallback())

    /**
     *订阅个人消息
     */
    fun subscribeTocUser()

    /**
     * 取消订阅个人消息
     */
    fun unsubscribeTocUser()
}