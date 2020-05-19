package ydk.mqtt.engine.observer

import ydk.mqtt.model.message.ChatRoomMessage
import ydk.mqtt.model.message.TopicRule


class ChatRoomServiceObserverImpl : ChatRoomServiceObserver {

    private val observerMsgMap = mutableMapOf<String, Observer<List<ChatRoomMessage>>>()


    override fun observeReceiveMessage(roomId: String, observer: Observer<List<ChatRoomMessage>>, register: Boolean) {
        var topicStr = "${TopicRule.TOPIC_TO_CLIENT_CHATROOM}$roomId"
        if (register) {
            observerMsgMap[topicStr] = observer
        } else {
            observerMsgMap.remove(topicStr)
        }
    }

    override fun dispatchReceiveMessage(topicStr: String, list: MutableList<ChatRoomMessage>) {
        if (list.isEmpty()) {
            return
        }
        var observer = observerMsgMap[topicStr]
        observer?.run {
            this.onEvent(list)
        }
    }


}