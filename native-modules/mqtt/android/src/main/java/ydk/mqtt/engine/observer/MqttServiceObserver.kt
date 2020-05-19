package ydk.mqtt.engine.observer

import ydk.mqtt.model.MqttStatus
import ydk.mqtt.proxy.ObserverAnnotation


@ObserverAnnotation(clazz = MqttServiceObserverImpl::class)
interface MqttServiceObserver : IMObserver {
    /**
     * 注册MQTT 在线事件
     */
    fun observeOnlineStatus(observer: Observer<MqttStatus>, register: Boolean)

    /**
     * 分发MQTT在线事件
     */
    fun dispatcOnlineStatus(mqttStatus: MqttStatus)

}