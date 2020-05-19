package ydk.mqtt

import ydk.mqtt.engine.observer.IMObserver
import ydk.mqtt.engine.service.IMServer
import ydk.mqtt.proxy.MqttServerProxy

object IMClient {

    /**
     * 获取客户端观察者代理
     * @param 代理接口的 class
     * @param 代理类的实际类
     */
    fun <T : IMObserver> getIMObserver(clazz: Class<T>): T {

        return MqttServerProxy.getIMObserver(clazz)
    }

    /**
     * 获取客服端服务的代理
     * @param 代理接口的 class
     * @return 代理类的实际类
     */
    fun <T : IMServer> getIMServer(clazz: Class<T>): T {

        return MqttServerProxy.getIMServer(clazz)
    }

}