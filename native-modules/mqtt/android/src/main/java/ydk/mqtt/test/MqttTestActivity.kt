package ydk.mqtt.test

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_mqtt_test.*
import ydk.mqtt.*
import ydk.mqtt.engine.observer.ChatRoomServiceObserver
import ydk.mqtt.engine.observer.MessageObserver
import ydk.mqtt.model.modelbuild.MessageBuild
import ydk.mqtt.model.message.parser.DefaultMsgExtAttachmentParser
import ydk.mqtt.model.message.attachment.TestExtAttachment
import ydk.mqtt.engine.service.ChatRoomService
import ydk.mqtt.engine.service.MessageService
import ydk.mqtt.engine.observer.Observer
import ydk.mqtt.model.message.ChatRoomMessage
import ydk.mqtt.model.message.P2PMessage
import ydk.mqtt.model.message.TopicRule

class MqttTestActivity : AppCompatActivity(), View.OnClickListener {

    private var subscribe_top = "${TopicRule.TOPIC_TO_CLIENT_CHATROOM}123456"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mqtt_test)

        connectMqtt.setOnClickListener(this)
        disconnect.setOnClickListener(this)
        publish.setOnClickListener(this)
        subscribe.setOnClickListener(this)
        unsubscribe.setOnClickListener(this)
        subscribe_user.setOnClickListener(this)
        unsubscribe_user.setOnClickListener(this)
        publish_user.setOnClickListener(this)

        var messageService = IMClient.getIMServer(MessageService::class.java)
        messageService.registerMsgExtAttachmentParser(DefaultMsgExtAttachmentParser())

        var observer = object : Observer<List<ChatRoomMessage>> {
            override fun onEvent(t: List<ChatRoomMessage>) {

                Log.e("observer", "ChatRoomMessage 收到夏新 消息 " + t.size)
                for (m in t) {
                    var ext = m.ext
                    Log.e("observer", "ChatRoomMessage 收到夏新 消息 mm " + m.attach)
                    Log.e("observer", "ChatRoomMessage 收到夏新 消息 ext " + ext?.javaClass?.simpleName)
                }
            }
        }
        IMClient.getIMObserver(ChatRoomServiceObserver::class.java).observeReceiveMessage("123456", observer)


        var observer2 = object : Observer<List<P2PMessage>> {
            override fun onEvent(t: List<P2PMessage>) {

                Log.e("observer2", "P2PMessage 收到夏新 消息 " + t.size)
                for (m in t) {
                    var ext = m.ext
                    Log.e("observer2", "P2PMessage 收到夏新 消息 mm " + m.attach)
                    Log.e("observer2", "P2PMessage 收到夏新 消息 ext " + ext?.javaClass?.simpleName)
                }
            }
        }
        IMClient.getIMObserver(MessageObserver::class.java).observeReceiveMessage("test", observer2)


    }


    override fun onClick(v: View) {
        when (v.id) {
            R.id.connectMqtt -> {

                connectMqtt()
            }
            R.id.disconnect -> {
                disconnect()
            }
            R.id.publish -> {
                var server = IMClient.getIMServer(ChatRoomService::class.java)

                var testExtAttachment = TestExtAttachment()
                testExtAttachment.message = "自定义的扩展"
                var createChatRoomMessage = MessageBuild.createChatRoomMessage("123456", "我发送的消息 ", testExtAttachment)

                server.sendMessage(createChatRoomMessage)
            }
            R.id.subscribe -> {
                var server = IMClient.getIMServer(ChatRoomService::class.java)
                server.enterChatRoom("123456")
            }
            R.id.unsubscribe -> {
                var server = IMClient.getIMServer(ChatRoomService::class.java)
                server.exitChatRoom("123456")
            }
            R.id.publish_user -> {

                var server = IMClient.getIMServer(MessageService::class.java)

                var testExtAttachment = TestExtAttachment()
                testExtAttachment.message = "自定义的扩展"
                var p2PMessage = MessageBuild.createP2PMessage(123456, "我发送的消息 ", testExtAttachment)
                server.sendP2PMessage(p2PMessage, SimpleRequestCallback())

            }
            R.id.subscribe_user -> {
                var server = IMClient.getIMServer(MessageService::class.java)
                server.subscribeTocUser()
            }
            R.id.unsubscribe_user -> {
                var server = IMClient.getIMServer(MessageService::class.java)
                server.unsubscribeTocUser()
            }
        }
    }

    private fun connectMqtt() {

        MqttClient.connectMqtt()
    }

    private fun disconnect() {
        MqttClient.disconnect()
    }


}
