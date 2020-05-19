package ydk.mqtt.engine.observer


interface Observer<T> {

    fun onEvent(t: T)
}