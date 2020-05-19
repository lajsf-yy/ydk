package ydk.mqtt.model.message.parser

import ydk.mqtt.model.message.attachment.ExtAttachment

interface MsgExtAttachmentParser {

    /**
     * 消息转化器
     * @param msgType 消息的类型
     * @param attach 消息的扩展字段
     *
     */
    fun serializeMsgExtAttachment(msgType: String, attach: String): ExtAttachment?


    /**
     * 转化的自定义扩展消息的类型
     *@return set集合
     */
    fun getMsgTypeSet(): MutableSet<String>

}