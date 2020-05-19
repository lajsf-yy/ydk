package ydk.mqtt.model.message.parser

import ydk.mqtt.model.message.attachment.ExtAttachment
import ydk.mqtt.model.message.attachment.TestExtAttachment

class DefaultMsgExtAttachmentParser : MsgExtAttachmentParser {


    override fun getMsgTypeSet(): MutableSet<String> = mutableSetOf("test")

    override fun serializeMsgExtAttachment(msgType: String, attach: String): ExtAttachment? {

        var extAttachment: ExtAttachment? = null

        if (msgType == "test") {
            extAttachment = TestExtAttachment()
        }
        extAttachment?.run {
            parseData(attach)
        }

        return extAttachment

    }

}