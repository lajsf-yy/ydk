package ydk.mqtt.engine.observer

import ydk.mqtt.model.message.P2PMessage
import ydk.mqtt.proxy.ObserverAnnotation


@ObserverAnnotation(clazz = MessageObserverImpl::class)
interface MessageObserver : IMObserver {

    /**
     * 注册/注销消息接收观察者。
     *
     * @param key
     * @param observer 观察者，参数为收到的消息集合
     * @param register true为注册，false为注销
     */
    fun observeReceiveMessage(key:String,observer: Observer<List<P2PMessage>>, register: Boolean = true)

    /**
     * 分发收到的P2PMessage消息
     *
     * @param list 消息
     */
    fun dispatchReceiveMessage(list: MutableList<P2PMessage>)

}