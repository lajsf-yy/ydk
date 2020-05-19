package ydk.mqtt.engine.observer

import ydk.mqtt.model.message.ChatRoomMessage
import ydk.mqtt.proxy.ObserverAnnotation

@ObserverAnnotation(clazz = ChatRoomServiceObserverImpl::class)
interface ChatRoomServiceObserver : IMObserver {


    /**
     * 注册/注销消息接收观察者。
     *
     * @param roomId 聊天室的ID
     * @param observer 观察者，参数为收到的消息集合
     * @param register true为注册，false为注销
     */
    fun observeReceiveMessage(roomId: String, observer: Observer<List<ChatRoomMessage>>, register: Boolean =true)

    /**
     * 分发收到的消息
     * @param topicStr 消息渠道
     * @param list 消息
     */
    fun dispatchReceiveMessage(topicStr: String, list: MutableList<ChatRoomMessage>)

}