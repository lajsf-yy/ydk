package ydk.mqtt.engine.observer

import ydk.mqtt.model.MqttStatus

class MqttServiceObserverImpl : MqttServiceObserver {


    private var observerList = mutableSetOf<Observer<MqttStatus>>()

    override fun observeOnlineStatus(observer: Observer<MqttStatus>, register: Boolean) {
        if (register) {
            observerList.add(observer)
        } else {
            observerList.remove(observer)
        }
    }

    override fun dispatcOnlineStatus(mqttStatus: MqttStatus) {

        for (observer in observerList) {
            observer.onEvent(mqttStatus)
        }
    }


}