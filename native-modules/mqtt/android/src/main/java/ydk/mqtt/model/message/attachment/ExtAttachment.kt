package ydk.mqtt.model.message.attachment

import java.io.Serializable

interface ExtAttachment : Serializable {
    /**
     * 序列化
     */
    fun parseData(josn: String)

    /**
     * 反序列化
     */
    fun dataPase(): String

    /**
     * 获取消息类型
     */
    fun getMsgType(): String
}