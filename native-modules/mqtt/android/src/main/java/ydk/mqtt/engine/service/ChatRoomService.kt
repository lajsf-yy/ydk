package ydk.mqtt.engine.service

import ydk.mqtt.RequestCallback
import ydk.mqtt.SimpleRequestCallback
import ydk.mqtt.model.message.ChatRoomMessage
import ydk.mqtt.proxy.ServerAnnotation

@ServerAnnotation(clazz = ChatRoomServiceImpl::class)
interface ChatRoomService : IMServer {
    /**
     * 发送聊天室消息 运用的MQTT发送
     * @param msg 消息
     * @param callback 状态回调者
     */
    fun sendMessage(msg: ChatRoomMessage, callback: RequestCallback<Void> = SimpleRequestCallback())

    /**
     * 发送聊天室消息 运用的HTTP发送
     * @param msg 消息
     * @param callback 状态回调者
     */
    fun sendMessage2(msg: ChatRoomMessage, callback: RequestCallback<Void> = SimpleRequestCallback())

    /**
     * 加入聊天室
     * @param roomId 聊天室ID
     * @param callback 状态回调者
     */
    fun enterChatRoom(roomId: String, callback: RequestCallback<Void> = SimpleRequestCallback())

    /**
     * 离开聊天室
     *@param roomId 聊天室ID
     */
    fun exitChatRoom(roomId: String)
}