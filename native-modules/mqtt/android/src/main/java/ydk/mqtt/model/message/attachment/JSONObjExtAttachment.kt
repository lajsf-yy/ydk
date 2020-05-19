package ydk.mqtt.model.message.attachment

import org.json.JSONObject

class JSONObjExtAttachment : ExtAttachment {

    var jSONObject: JSONObject? = null

    private var msgType: String = ""

    override fun parseData(josn: String) {
        jSONObject = JSONObject(josn)
    }

    override fun dataPase(): String {
        return jSONObject?.run {
            this.toString()
        } ?: JSONObject().toString()
    }

    override fun getMsgType(): String = this.msgType

    /**
     * 设置消息额type
     *
     * 该方法，只针对 JSONObjMsgExtAttachmentParser
     */
    fun setMsgType(msgType: String) {
        this.msgType = msgType
    }

}