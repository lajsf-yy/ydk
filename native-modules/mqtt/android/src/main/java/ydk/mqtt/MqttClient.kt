package ydk.mqtt

import com.yryz.network.http.token.TokenCache
import org.fusesource.mqtt.client.Callback
import ydk.mqtt.model.MqttStatus
import ydk.mqtt.mq.MQTTManage
import ydk.mqtt.mq.MqttConfig


object MqttClient {

    /**
     * 获取mqtt 的链接状态
     *
     * @return MqttStatus
     */
    fun getMqttStatus(): MqttStatus = MQTTManage.get().getMqttStatus()


    /**
     * 获取连接参数
     *
     * @return MqttConfig 连接参数
     */
    fun getMqttConfig(): MqttConfig {

        //  var httpHeader = TokenCache.getHttpHeader()

        //TODO 测试代码
        val host = "192.168.30.34"

        val port = 1883

        //val userId = httpHeader.userId ?: ""
        val userId = "1234567890"
        // val userToken = httpHeader.token ?: ""
        val userToken = "123456"

        return MqttConfig(host, port, userId, userToken)
    }

    /**
     * 创建MQTT 链接
     */
    fun connectMqtt() {

        MQTTManage.get().connectMqtt()
    }

    /**
     * 断开mqtt 链接
     */
    fun disconnect() {

        MQTTManage.get().disconnect()
    }

    /**
     * 发送消息
     *
     * @param top 主题集
     * @param content 消息
     * @param callback 回调
     */
    fun publish(top: String, content: String, callback: Callback<Void>? = null) {

        MQTTManage.get().publish(top, content, callback)
    }

    /**
     * 订阅主题
     *
     * @param tops 主题集合
     * @param callback 回调
     */
    fun subscribe(vararg tops: String, callback: Callback<ByteArray>? = null) {

        MQTTManage.get().subscribe(*tops, callback = callback)
    }

    /**
     * 取消订阅主题
     *
     * @param tops 主题集合
     * @param callback 回调
     */
    fun unsubscribe(vararg tops: String, callback: Callback<Void>? = null) {

        MQTTManage.get().unsubscribe(*tops, callback = callback)
    }
}