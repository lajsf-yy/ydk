package com.mqtt

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.yryz.network.http.network.NeetWork
import com.yryz.network.http.token.TokenController
import org.fusesource.hawtbuf.Buffer
import org.fusesource.hawtbuf.UTF8Buffer
import org.fusesource.hawtbuf.AsciiBuffer
import org.fusesource.mqtt.client.*
import org.fusesource.mqtt.codec.CONNACK
import ydk.mqtt.IMClient
import ydk.mqtt.MqttClient
import ydk.mqtt.engine.buffer.MessageBuffer
import ydk.mqtt.engine.observer.MqttServiceObserver
import ydk.mqtt.model.MqttStatus
import ydk.mqtt.model.StatusCode
import java.io.EOFException


class MQTTConnection {

    private var connection: CallbackConnection? = null

    private val handler: Handler = Handler(Looper.getMainLooper())

    private var mqttStatus: MqttStatus = MqttStatus(StatusCode.INVALID)

    private var messageBuffer: MessageBuffer? = null


    companion object {

        private val MQOS: QoS = QoS.AT_MOST_ONCE
    }

    /**
     * 设置MQTT 的链接状态
     * @param statusCode 链接状态
     */
    private fun setMqttStatusCode(statusCode: StatusCode) {
        mqttStatus.statusCode = statusCode
    }

    /**
     * 获取MqttStatus
     *
     */
    fun getMqttStatus(): MqttStatus = mqttStatus

    /**
     *
     * @return mqtt 的 链接状态
     */
    fun getMqttStatusCode(): StatusCode {

        return mqttStatus.statusCode
    }


    private fun log(message: String) {
        Log.e("MQTTClient", message)
    }

    /**
     * 创建
     */
    internal fun creatMQTT() {
        var mqttConfig = MqttClient.getMqttConfig()
        var mqtt = MQTT()
        mqtt.run {
            //mqtt 端口号和ip
            setHost("tcp://${mqttConfig.host}:${mqttConfig.port}")
            // 用户名为 userId
            setUserName(mqttConfig.userId)
            //密码为token
            setPassword(mqttConfig.userToken)
            //ClientId
            setClientId("${mqttConfig.userId}@APP")

            //设置从新连接的次数
            // reconnectAttemptsMax = 10
            // 设置重连的事件间隔
            // reconnectDelay
            //设置心跳时间 默认30
            //keepAlive = 30

        }
        //消息缓冲
        messageBuffer = MessageBuffer()

        setMqttStatusCode(StatusCode.INVALID)

        connection = mqtt.callbackConnection()
        connection!!.listener(object : Listener {
            override fun onFailure(value: Throwable?) {
                log("listener onFailure ${value?.message}")
                // TODO
                handler.post {
                    setMqttStatusCode(StatusCode.CONNECT_ERROR)
                    var mqttStatus = MqttStatus(StatusCode.CONNECT_ERROR)
                    IMClient.getIMObserver(MqttServiceObserver::class.java).dispatcOnlineStatus(mqttStatus)
                }

            }

            override fun onPublish(topic: UTF8Buffer?, body: Buffer?, ack: Runnable?) {

                log("listener onPublish ")

                var topicByteArray = topic?.toByteArray()

                var bodyByteArray = body?.toByteArray()

                var topicStr = if (topicByteArray != null) String(topicByteArray) else ""
                var bodyStr = if (bodyByteArray != null) String(bodyByteArray) else ""
                log("creatMQTT topicStr $topicStr")
                log("creatMQTT bodyStr $bodyStr")
                if (topicStr.isEmpty() || bodyStr.isEmpty()) {
                    return
                }

                messageBuffer?.addBuffer(ydk.mqtt.engine.buffer.Buffer(topicStr, bodyStr))

            }

            override fun onConnected() {
                log("listener onConnected ")
                handler.post {
                    setMqttStatusCode(StatusCode.CONNECT)
                    //分发 mqtt 的状态
                    var mqttStatus = MqttStatus(StatusCode.CONNECT)
                    IMClient.getIMObserver(MqttServiceObserver::class.java).dispatcOnlineStatus(mqttStatus)
                }

            }

            override fun onDisconnected() {
                log("listener onDisconnected ")
                handler.post {
                    setMqttStatusCode(StatusCode.DISCONNECT)
                    var mqttStatus = MqttStatus(StatusCode.DISCONNECT)
                    IMClient.getIMObserver(MqttServiceObserver::class.java).dispatcOnlineStatus(mqttStatus)
                }
            }
        })
        /**
         * 网诺监听
         */
        NeetWork.registerListener("ydk-mqtt") {

        }

    }

    /**
     * 链接
     */
    internal fun connect() {

        var call = object : Callback<Void> {
            override fun onSuccess(value: Void?) {
                log("connect onSuccess ")

            }

            override fun onFailure(value: Throwable?) {
                log("connect onFailure ${value?.message}")
                handleConnectError(value)
            }
        }

        connection?.run {
            this.connect(call)
        }
    }

    /**
     * 断开链接
     */
    internal fun disconnect(callback: (() -> Unit) = {}) {

        var call = object : Callback<Void> {
            override fun onSuccess(value: Void?) {
                log(" disconnect onSuccess")
                messageBuffer?.stopBuffer()
                callback()
            }

            override fun onFailure(value: Throwable?) {
                log(" disconnect onFailure ${value?.message}")
                messageBuffer?.stopBuffer()
                callback()

            }
        }
        connection?.run {
            this.disconnect(call)
        }

        NeetWork.unrRegisterListener("ydk-mqtt")
    }

    /**
     * 发送消息
     * @param top 订阅的主题
     * @param content 消息类容
     */
    internal fun publish(top: String, content: String, callback: Callback<Void>? = null) {

        var call = object : Callback<Void> {

            override fun onSuccess(value: Void?) {
                log("publish  onSuccess ")
                callback?.run {
                    handler.post {
                        this.onSuccess(value)
                    }
                }
            }

            override fun onFailure(value: Throwable?) {
                log("publish  onFailure ${value?.message}")
                callback?.run {
                    handler.post {
                        this.onFailure(value)
                    }
                }
            }
        }
        connection?.run {
            val topic = UTF8Buffer(top)
            val msg = AsciiBuffer(content)
            this.publish(topic, msg, MQOS, false, call)
        }
    }

    /**
     * 订阅
     * * @param tops 订阅的主题
     */
    internal fun subscribe(vararg tops: String, callback: Callback<ByteArray>? = null) {

        if (tops.isEmpty() || tops.isEmpty()) {
            return
        }

        var callback = object : Callback<ByteArray> {
            override fun onSuccess(qoses: ByteArray) {
                log(" subscribe onSuccess ")
                callback?.run {
                    handler.post {
                        this.onSuccess(qoses)
                    }
                }
            }

            override fun onFailure(value: Throwable) {
                log(" subscribe onFailure ${value.message}")
                callback?.run {
                    handler.post {
                        this.onFailure(value)
                    }
                }
            }
        }

        connection?.run {
            var topics = arrayOfNulls<Topic>(tops.size)
            for ((index, top) in tops.withIndex()) {
                var topic = Topic(top, MQOS)
                topics[index] = topic
            }
            this.subscribe(topics, callback)
        }
    }

    /**
     * 取消订阅
     * @param tops 订阅的主题
     */
    internal fun unsubscribe(vararg tops: String, callback: Callback<Void>? = null) {
        if (tops.isEmpty()) {
            return
        }
        var callback = object : Callback<Void> {
            override fun onSuccess(value: Void?) {
                log(" unsubscribe onSuccess ")
                callback?.run {
                    handler.post {
                        this.onSuccess(value)
                    }
                }

            }

            override fun onFailure(value: Throwable?) {
                log(" unsubscribe onFailure ${value?.message}")
                callback?.run {
                    handler.post {
                        this.onFailure(value)
                    }
                }
            }
        }

        connection?.run {
            var topics = arrayOfNulls<UTF8Buffer>(tops.size)
            for ((index, top) in tops.withIndex()) {
                val utF8Buffer = UTF8Buffer(top)
                topics[index] = utF8Buffer
            }
            this.unsubscribe(topics, callback)
        }
    }

    /**
     * 错误统一处理 TODO
     * @param throwable 异常信息
     */
    private fun handleConnectError(throwable: Throwable?) {
        if (throwable == null) return

        when (throwable) {
            is MQTTException -> {
                var connack = throwable.connack
                var code = connack.code()
                when (code) {
                    //鉴权失败
                    CONNACK.Code.CONNECTION_REFUSED_NOT_AUTHORIZED -> {
                        //断开链接，触发 EOFException,则会走 EOFException 分支
                        disconnect {
                            newToken()
                        }

                    }
                    //错误的用户名或者密码
                    CONNACK.Code.CONNECTION_REFUSED_BAD_USERNAME_OR_PASSWORD -> {
                        handler.post {
                            setMqttStatusCode(StatusCode.PWD_ERROR)
                            var mqttStatus = MqttStatus(StatusCode.PWD_ERROR)
                            IMClient.getIMObserver(MqttServiceObserver::class.java).dispatcOnlineStatus(mqttStatus)
                        }
                    }
                    //其他错误
                    else -> {

                    }
                }

            }
            is EOFException -> {
                disconnect {
                    newToken()
                }
            }
            else -> {

            }
        }
    }

    /**
     * 重新处理token
     */
    private fun newToken() {
        TokenController.handlerToken(102).subscribe() {
            if (it) {
                handler.postDelayed({
                    //重新链接mqtt
                    creatMQTT()
                    connect()
                }, 1000)
            }
        }
    }

}