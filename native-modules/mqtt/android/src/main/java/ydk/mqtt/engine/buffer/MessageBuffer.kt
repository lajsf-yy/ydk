package ydk.mqtt.engine.buffer

import android.os.*
import ydk.mqtt.IMClient
import ydk.mqtt.engine.observer.ChatRoomServiceObserver
import ydk.mqtt.engine.observer.MessageObserver
import ydk.mqtt.model.modelbuild.MessageBuild
import ydk.mqtt.model.message.ChatRoomMessage
import ydk.mqtt.model.message.IMMessage
import ydk.mqtt.model.message.P2PMessage
import ydk.mqtt.model.message.TopicRule
import java.io.Serializable

data class Buffer(var topicStr: String, var bodyStr: String) : Serializable

/**
 * 消息加入 buffer容器
 */
private const val WHAT_1000 = 1000
/**
 * 消息处理完毕
 */
private const val WHAT_1001 = 1001
/**
 * 停止缓冲队列
 */
private const val WHAT_1010 = 1010


/**
 * 消息处理器
 */
private class BufferExecuteTask(var handler: Handler) : AsyncTask<List<Buffer>, Void, MutableMap<String, MutableList<IMMessage>>>() {

    var messageMap = mutableMapOf<String, MutableList<IMMessage>>()

    override fun doInBackground(vararg params: List<Buffer>?): MutableMap<String, MutableList<IMMessage>> {

        var list = params?.get(0) ?: emptyList()

        for (buffer in list) {
            var topicStr = buffer.topicStr
            var bodyStr = buffer.bodyStr

            when {
                //聊天室消息
                topicStr.contains(TopicRule.TOPIC_TO_CLIENT_CHATROOM) -> {
                    var chatRoomMessage = MessageBuild.createChatRoomMessageByJson(bodyStr)

                    putMessage(topicStr, chatRoomMessage)
                }
                //个人类型的消息
                topicStr.contains(TopicRule.TOPIC_TO_CLIENT_USER) -> {

                    var p2PMessage = MessageBuild.createP2PMessageByJson(bodyStr)

                    putMessage(topicStr, p2PMessage)

                }
                //本地广播
                topicStr.contains(TopicRule.TOPIC_TO_CLIENT_BROADCAST) -> {

                }
                else -> {

                }
            }
        }
        return messageMap
    }

    /**
     * put message
     *
     * @param topicStr 消息渠道
     * @param message 消息
     *
     */
    private fun putMessage(topicStr: String, message: IMMessage) {
        var messageList = messageMap[topicStr]
        if (messageList == null) {
            messageList = mutableListOf()
            messageMap[topicStr] = messageList
        }
        messageList.add(message)
    }

    /**
     *
     */
    private fun <T : IMMessage> asMessage(messages: MutableList<IMMessage>): MutableList<T> {
        val list = mutableListOf<T>()
        for (message in messages) {
            list.add(message as T)
        }
        return list
    }


    override fun onPostExecute(result: MutableMap<String, MutableList<IMMessage>>) {


        var iterator = result.iterator()
        while (iterator.hasNext()) {
            var next = iterator.next()
            var topicStr = next.key
            var list = next.value

            when {
                //聊天室消息
                topicStr.contains(TopicRule.TOPIC_TO_CLIENT_CHATROOM) -> {
                    var asMessage = asMessage<ChatRoomMessage>(list)
                    IMClient.getIMObserver(ChatRoomServiceObserver::class.java).dispatchReceiveMessage(topicStr, asMessage)
                }
                //个人类型的消息
                topicStr.contains(TopicRule.TOPIC_TO_CLIENT_USER) -> {
                    var asMessage = asMessage<P2PMessage>(list)
                    IMClient.getIMObserver(MessageObserver::class.java).dispatchReceiveMessage(asMessage)

                }
                //广播类型的消息
                topicStr.contains(TopicRule.TOPIC_TO_CLIENT_BROADCAST) -> {

                }
                else -> {

                }
            }
        }

    }

}


/**
 * 消息缓冲线程
 */
private class BufferTask : Thread() {
    /**
     * 数据缓冲集合
     */
    private val bufferList: MutableList<Buffer> = mutableListOf()
    /**
     * 子线程的 hadler
     */
    private var handler: Handler? = null
    /**
     * 消息的处理数量
     * 每次100条
     */
    private val bufferCount = 100
    /**
     * 锁
     */
    private var lock = false


    override fun run() {
        Looper.prepare()
        handler = object : Handler() {
            override fun handleMessage(msg: Message?) {
                if (msg == null) {
                    return
                }
                var what = msg.what
                when (what) {
                    WHAT_1000 -> {

                        var buffer = msg.obj as Buffer

//                        for (index in 0 until 20000) {
//                            var js = JSONObject(buffer.bodyStr)
//                            js.put("attach", "消息正文 ### " + index)
//
//                            var bufferc = Buffer(buffer.topicStr, js.toString())
//                            bufferList.add(bufferc)
//                        }
                        bufferList.add(buffer)
                        dispatchBuffer()
                    }

                    WHAT_1001 -> {
                        //前一次消息处理完毕，处理下一次消息
                        lock = false
                        dispatchBuffer()
                    }
                    WHAT_1010 -> {
                        bufferList.clear()
                        removeMessages(WHAT_1000)
                        removeMessages(WHAT_1001)
                        looper.quit()
                        try {
                            currentThread().interrupt()
                        } catch (e: Exception) {

                        }
                    }
                    else -> {

                    }
                }
            }
        }
        Looper.loop()
    }

    /**
     * 分发消息
     */

    private fun dispatchBuffer() {

        if (bufferList.isEmpty()) {
            return
        }
        if (lock) {
            return
        }
        lock = true

        var list: MutableList<Buffer> = mutableListOf()
        if (bufferList.size > bufferCount) {
            var subLists = bufferList.subList(0, bufferCount)
            list.addAll(subLists)
            bufferList.removeAll(list)

        } else {
            list.addAll(bufferList)
            bufferList.clear()
        }
        if (list.isNotEmpty()) {
            BufferExecuteTask(handler!!).execute(list)
        } else {
            lock = false
        }
    }

    fun addBuffer(buffer: Buffer) {

        var message: Message = Message.obtain()
        message.what = WHAT_1000
        message.obj = buffer
        handler?.sendMessage(message)
    }

    fun stopBuffer() {

        handler?.sendEmptyMessage(WHAT_1010)
    }

}

/**
 * 消息缓冲器
 */
class MessageBuffer {

    private var bufferTask: BufferTask = BufferTask()

    init {
        bufferTask.start()
    }

    /**
     * 添加消息到缓冲队列
     *
     * @param buffer
     */
    fun addBuffer(buffer: Buffer) {

        bufferTask.addBuffer(buffer)

    }

    /**
     * 停止消息队列
     */
    fun stopBuffer() {

        bufferTask.stopBuffer()
    }


}