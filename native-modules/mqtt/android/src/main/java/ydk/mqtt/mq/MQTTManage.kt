package ydk.mqtt.mq

import com.mqtt.MQTTConnection
import org.fusesource.mqtt.client.Callback
import ydk.mqtt.model.MqttStatus
import ydk.mqtt.model.StatusCode


class MQTTManage private constructor() {
    /**
     * mqtt 客户端
     */
    private var mMQTTConnection: MQTTConnection = MQTTConnection()

    companion object {

        private var instance: MQTTManage? = null
            get() {
                if (field == null) {
                    field = MQTTManage()
                }
                return field
            }

        internal fun get(): MQTTManage {

            return instance!!
        }
    }

    /**
     * 获取mqtt 的链接状态
     */
    internal fun getMqttStatus(): MqttStatus = mMQTTConnection.getMqttStatus()

    internal fun connectMqtt() {


        if (mMQTTConnection.getMqttStatusCode() == StatusCode.CONNECT) {

            return
        }
        // var mqttConfig = MqttConfig("192.168.56.1", 1883, "admin", "password")
        //   var mqttConfig = MqttConfig("192.168.30.34", 1883, "admin", "public")

        mMQTTConnection.creatMQTT()

        mMQTTConnection.connect()

    }

    internal fun disconnect() {

        mMQTTConnection.disconnect()
    }

    internal fun publish(top: String, content: String, callback: Callback<Void>? = null) {

        mMQTTConnection.publish(top, content, callback)
    }


    internal fun subscribe(vararg tops: String, callback: Callback<ByteArray>? = null) {

        mMQTTConnection.subscribe(*tops, callback = callback)
    }


    internal fun unsubscribe(vararg tops: String, callback: Callback<Void>? = null) {

        mMQTTConnection.unsubscribe(*tops, callback = callback)

    }


}