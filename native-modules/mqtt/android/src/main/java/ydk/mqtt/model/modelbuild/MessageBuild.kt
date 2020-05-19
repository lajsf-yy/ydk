package ydk.mqtt.model.modelbuild

import org.json.JSONObject
import ydk.mqtt.IMClient
import ydk.mqtt.engine.service.MessageService
import ydk.mqtt.model.message.ChatRoomMessage
import ydk.mqtt.model.message.IMMessage
import ydk.mqtt.model.message.P2PMessage
import ydk.mqtt.model.message.attachment.ExtAttachment
import java.util.*

object MessageBuild {

    /**###################################################
     *
     *
     * ChatRoomMessage
     *
     * 以下为聊天室消息的封装
     *
     *
     *
     * ###################################################
     * **/

    /**
     *创建聊天室消息
     *
     * @param roomId 聊天室ID
     * @param attach 消息的正文本
     * @param ext 消息的扩展字段
     * @return 聊天室消息
     */
    fun createChatRoomMessage(roomId: String, attach: String = "", ext: ExtAttachment? = null): ChatRoomMessage {

        var chatRoomMessage = ChatRoomMessage()

        chatRoomMessage.roomId = roomId

        initIMMessage(chatRoomMessage, attach, ext)

        return chatRoomMessage
    }

    /**
     *JSON转聊天室消息
     *
     *@param json 服务端发送过来的消息对象
     *@return chatRoomMessage 对象
     */
    fun createChatRoomMessageByJson(json: String): ChatRoomMessage {

        var chatRoomMessage = ChatRoomMessage()

        var jsonObject = JSONObject(json)

        initIMMessageByJson(chatRoomMessage, jsonObject)

        chatRoomMessage.roomId = jsonObject.optString("roomId")

        chatRoomMessage.fromUname = jsonObject.optString("fromUname")

        return chatRoomMessage
    }


    /**
     *聊天室消息转JSON
     *
     *@param chatRoomMessage 聊天室消息
     *@return 聊天室消息的JSON 对象
     */
    fun createChatRoomMessageJson(chatRoomMessage: ChatRoomMessage): String {

        var initIMMessageJson = initIMMessageJson(chatRoomMessage)
        initIMMessageJson.put("roomId", chatRoomMessage.roomId)

        return initIMMessageJson.toString()
    }


    /**###################################################
     *
     *
     * P2PMessage
     *
     * 以下为P2PMessage消息的封装
     *
     *
     *
     * ###################################################
     * **/

    /**
     * @param toUid 消息的接受者ID
     * @param attach 消息的文本
     * @param  ext 消息的附件
     * @return P2PMessage
     */
    fun createP2PMessage(toUid: Long, attach: String = "", ext: ExtAttachment? = null): P2PMessage {

        var p2PMessage = P2PMessage()

        initIMMessage(p2PMessage, attach, ext)

        p2PMessage.toUid = toUid


        return p2PMessage
    }

    /**
     *json 转 P2PMessage
     *
     *@param json 服务端发送过来的消息对象
     *
     *@return P2PMessage 对象
     */
    fun createP2PMessageByJson(json: String): P2PMessage {

        var p2PMessage = P2PMessage()

        var jsonObject = JSONObject(json)

        initIMMessageByJson(p2PMessage, jsonObject)

        p2PMessage.toUid = jsonObject.optLong("toUid")

        return p2PMessage
    }


    /**
     *聊天室消息转JSON
     *
     *@param chatRoomMessage 聊天室消息
     *@return 聊天室消息的JSON 对象
     */
    fun createP2PMessageJson(p2PMessage: P2PMessage): String {

        var initIMMessageJson = initIMMessageJson(p2PMessage)

        initIMMessageJson.put("toUid", p2PMessage.toUid)

        return initIMMessageJson.toString()
    }

    /**###################################################
     *
     *
     * IMMessage
     *
     * 以下为IMMessage消息的公共封装
     *
     *
     *
     * ###################################################
     * **/

    /**
     * 初始化消息的基础字段
     * 转为为本地消息字段
     */
    private fun initIMMessage(message: IMMessage, attach: String = "", ext: ExtAttachment? = null) {
        //TODO
        message.run {
            this.fromUid = 0
            this.msgId = getUUID()
            this.attach = attach
            this.msgType = ext?.getMsgType() ?: "0"
            this.ext = ext
        }
    }

    /**
     * 初始化消息
     * 将服务端发送过来的消息字段转化为本地消息
     *
     */
    private fun initIMMessageByJson(message: IMMessage, josn: JSONObject) {

        fun getExt(msgType: String, ext: String): ExtAttachment? {

            return IMClient.getIMServer(MessageService::class.java).serializeMsgExtAttachment(msgType, ext)

        }

        var msgType = josn.optString("msgType")

        message.run {
            this.fromUid = josn.optLong("fromUid")
            this.msgId = josn.optString("msgId")
            this.attach = josn.optString("attach")
            this.msgType = msgType
            this.ext = getExt(msgType, josn.optString("ext"))
        }

    }

    /**
     * 初始化消息的基础字段
     * 将本地消息转化为发送给服务端的字段
     */
    private fun initIMMessageJson(message: IMMessage): JSONObject {
        var jsonObject = JSONObject()
        jsonObject.put("msgId", message.msgId)
        jsonObject.put("fromUid", message.fromUid)
        jsonObject.put("msgType", message.msgType)
        jsonObject.put("attach", message.attach)
        var ext = message.ext
        ext?.run {
            jsonObject.put("ext", this.dataPase())
        }
        return jsonObject
    }

    /**
     * 获取UUID
     *
     */
    private fun getUUID(): String {
        var randomUUID = UUID.randomUUID()
        var uuid = randomUUID.toString()
        return uuid.replace("-", "")
    }
}