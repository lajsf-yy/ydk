package ydk.mqtt.model.message.parser

import ydk.mqtt.model.message.attachment.ExtAttachment
import ydk.mqtt.model.message.attachment.JSONObjExtAttachment

class JSONObjMsgExtAttachmentParser : MsgExtAttachmentParser {

    override fun serializeMsgExtAttachment(msgType: String, attach: String): ExtAttachment? {

        val jSONObjExtAttachment = JSONObjExtAttachment()
        //解析数据
        jSONObjExtAttachment.parseData(attach)
        //设置消息的type
        jSONObjExtAttachment.setMsgType(msgType)

        return jSONObjExtAttachment
    }

    override fun getMsgTypeSet(): MutableSet<String> = mutableSetOf()


}