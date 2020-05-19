package ydk.mqtt.engine.service

import org.fusesource.mqtt.client.Callback
import ydk.mqtt.MqttClient
import ydk.mqtt.RequestCallback
import ydk.mqtt.model.message.TopicRule
import ydk.mqtt.model.modelbuild.MessageBuild
import ydk.mqtt.model.message.ChatRoomMessage

class ChatRoomServiceImpl : ChatRoomService {

    override fun enterChatRoom(roomId: String, callback: RequestCallback<Void>) {

        //TODO 接口调用 这里只有mqtt 的订阅

        var top = "${TopicRule.TOPIC_TO_CLIENT_CHATROOM}$roomId"
        MqttClient.subscribe(top, callback = object : Callback<ByteArray> {
            override fun onSuccess(value: ByteArray?) {
                callback.onSuccess()
            }

            override fun onFailure(value: Throwable?) {
                callback.onException(value)
            }
        })

    }

    override fun exitChatRoom(roomId: String) {

        //TODO 接口调用 这里只有mqtt 的取消订阅

        var top = "${TopicRule.TOPIC_TO_CLIENT_CHATROOM}$roomId"

        MqttClient.unsubscribe(top)
    }

    override fun sendMessage(msg: ChatRoomMessage, callback: RequestCallback<Void>) {

        var top = TopicRule.TOPIC_TO_SERVER_CHATROOM


        var messageJson = MessageBuild.createChatRoomMessageJson(msg)


        MqttClient.publish(top, messageJson, object : Callback<Void> {
            override fun onSuccess(value: Void?) {
                callback.onSuccess()
            }

            override fun onFailure(value: Throwable?) {
                callback.onException(value)
            }

        })

    }


    override fun sendMessage2(msg: ChatRoomMessage, callback: RequestCallback<Void>) {

    }


}