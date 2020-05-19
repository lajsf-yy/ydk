package ydk.mqtt.engine.observer

import ydk.mqtt.model.message.P2PMessage

class MessageObserverImpl : MessageObserver {


    private var observerMap: MutableMap<String, Observer<List<P2PMessage>>> = mutableMapOf()

    override fun observeReceiveMessage(key: String, observer: Observer<List<P2PMessage>>, register: Boolean) {
        if (register) {
            observerMap[key] = observer
        } else {
            observerMap.remove(key)
        }
    }

    override fun dispatchReceiveMessage(list: MutableList<P2PMessage>) {
        var iterator = observerMap.iterator()
        while (iterator.hasNext()) {
            var next = iterator.next()
            var value = next.value
            value.onEvent(list)
        }
    }

}