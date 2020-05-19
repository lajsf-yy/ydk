package ydk.mqtt.engine.service

import org.fusesource.mqtt.client.Callback
import ydk.mqtt.MqttClient
import ydk.mqtt.RequestCallback
import ydk.mqtt.model.modelbuild.MessageBuild
import ydk.mqtt.model.message.P2PMessage
import ydk.mqtt.model.message.TopicRule
import ydk.mqtt.model.message.attachment.ExtAttachment
import ydk.mqtt.model.message.parser.JSONObjMsgExtAttachmentParser
import ydk.mqtt.model.message.parser.MsgExtAttachmentParser

class MessageServiceImpl : MessageService {


    private var mutableSet: MutableSet<MsgExtAttachmentParser> = mutableSetOf()

    private var jSONObjMsgExtAttachmentParser: JSONObjMsgExtAttachmentParser = JSONObjMsgExtAttachmentParser()

    override fun registerMsgExtAttachmentParser(msgExtAttachmentParser: MsgExtAttachmentParser, register: Boolean) {

        if (register) {
            mutableSet.add(msgExtAttachmentParser)
        } else {
            mutableSet.remove(msgExtAttachmentParser)
        }
    }

    override fun serializeMsgExtAttachment(msgType: String, attach: String): ExtAttachment? {

        for (msgExtAttachmentParser in mutableSet) {
            var msgTypeSet = msgExtAttachmentParser.getMsgTypeSet()
            if (msgTypeSet.contains(msgType)) {
                return msgExtAttachmentParser.serializeMsgExtAttachment(msgType, attach)
            }
        }
        //没有注册解析器的实体，返回默认的JSON 解析器
        return jSONObjMsgExtAttachmentParser.serializeMsgExtAttachment(msgType, attach)
    }


    override fun sendP2PMessage(msg: P2PMessage, callback: RequestCallback<Void>) {
        //TODO
        var top = TopicRule.TOPIC_TO_SERVER_USER

        var messageJson = MessageBuild.createP2PMessageJson(msg)

        MqttClient.publish(top, messageJson, object : Callback<Void> {
            override fun onSuccess(value: Void?) {
                callback.onSuccess()
            }

            override fun onFailure(value: Throwable?) {
                callback.onException(value)
            }

        })
    }

    override fun sendP2PMessage2(msg: P2PMessage, callback: RequestCallback<Void>) {

    }

    override fun subscribeTocUser() {
        val mqttConfig = MqttClient.getMqttConfig()
        val top = "${TopicRule.TOPIC_TO_CLIENT_USER}${mqttConfig.userId}"
        MqttClient.subscribe(top)
    }

    override fun unsubscribeTocUser() {
        val mqttConfig = MqttClient.getMqttConfig()
        val top = "${TopicRule.TOPIC_TO_CLIENT_USER}${mqttConfig.userId}"
        MqttClient.unsubscribe(top)
    }
}