package ydk.mqtt

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.facebook.react.bridge.ReadableMap
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import ydk.annotations.YdkModule
import ydk.core.Ydk
import ydk.mqtt.engine.observer.ChatRoomServiceObserver
import ydk.mqtt.engine.observer.IMObserver
import ydk.mqtt.engine.observer.MessageObserver
import ydk.mqtt.engine.observer.Observer
import ydk.mqtt.engine.service.ChatRoomService
import ydk.mqtt.engine.service.MessageService
import ydk.mqtt.model.message.ChatRoomMessage
import ydk.mqtt.model.message.P2PMessage
import ydk.mqtt.model.message.attachment.JSONObjExtAttachment
import ydk.mqtt.model.message.attachment.TestExtAttachment
import ydk.mqtt.model.modelbuild.MessageBuild
import java.io.IOException
import java.lang.IllegalArgumentException

@YdkModule
class YdkMqtt {


    /**
     * 链接
     */
    fun connect(): Observable<Boolean> {


        return Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
            MqttClient.connectMqtt()
            emitter.onNext(true)
            emitter.onComplete()
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())

    }

    /**
     * 断开链接
     */
    fun disconnect(): Observable<Boolean> {

        return Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
            MqttClient.disconnect()
            emitter.onNext(true)
            emitter.onComplete()
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
    }

    /**
     * 订阅群组消息
     * @param subscribe 订阅
     * @param roomId 群组id
     */
    fun subscribeChatRoom(subscribe: Boolean, roomId: String): Observable<Boolean> {
        return Observable.create(ObservableOnSubscribe<Boolean> { emitter ->
            var imServer = IMClient.getIMServer(ChatRoomService::class.java)

            if (subscribe) {
                imServer.enterChatRoom(roomId, callback = object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {
                        emitter.onNext(true)
                        emitter.onComplete()
                    }

                    override fun onFailed(code: Int) {
                        emitter.onNext(false)
                        emitter.onComplete()
                    }

                    override fun onException(exception: Throwable?) {
                        emitter.onError(exception ?: IOException("subscribeChatRoom error"))
                    }
                })
            } else {
                imServer.exitChatRoom(roomId)
                emitter.onNext(true)
                emitter.onComplete()
            }
        })

    }

    /**
     * 发送P2PMessage 消息
     *
     * @param p2PMessageVo RN 端传过来的实体
     */
    fun sendP2PMessage(p2PMessageVo: ReadableMap): Observable<Boolean> {

        return Observable.create(object : ObservableOnSubscribe<Boolean> {
            override fun subscribe(emitter: ObservableEmitter<Boolean>) {
                var msgType = p2PMessageVo.getString("msgType")
                if (TextUtils.isEmpty(msgType)) {
                    emitter.onError(IllegalArgumentException("msgType 参数异常"))
                    return
                }

                var toUid = p2PMessageVo.getDouble("toUid")

                var attach = p2PMessageVo.getString("attach")

                var ext = p2PMessageVo.getMap("ext")

                var jsonObjExtAttachment = JSONObjExtAttachment()
                jsonObjExtAttachment.setMsgType(msgType!!)
                jsonObjExtAttachment.jSONObject = ext?.run {
                    var toHashMap = this.toHashMap()
                    JSONObject(toHashMap)
                } ?: JSONObject()

                var p2PMessage = MessageBuild.createP2PMessage(toUid.toLong(), attach
                        ?: "", jsonObjExtAttachment)

                var server = IMClient.getIMServer(MessageService::class.java)
                server.sendP2PMessage(p2PMessage, object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {
                        emitter.onNext(true)
                        emitter.onComplete()
                    }

                    override fun onFailed(code: Int) {
                        emitter.onNext(false)
                        emitter.onComplete()
                    }

                    override fun onException(exception: Throwable?) {
                        emitter.onError(exception ?: IOException())
                    }
                })
            }

        })


    }

    /**
     * 发送 ChatRoomMessage 消息
     *
     * @param chatRoomMessageVo RN 端传过来的实体
     */
    fun sendChatRoomMessage(chatRoomMessageVo: ReadableMap): Observable<Boolean> {

        return Observable.create(object : ObservableOnSubscribe<Boolean> {
            override fun subscribe(emitter: ObservableEmitter<Boolean>) {
                var msgType = chatRoomMessageVo.getString("msgType")
                if (TextUtils.isEmpty(msgType)) {
                    emitter.onError(IllegalArgumentException("msgType 参数异常"))
                    return
                }

                var roomId = chatRoomMessageVo.getString("roomId")
                if (TextUtils.isEmpty(roomId)) {
                    emitter.onError(IllegalArgumentException("roomId 参数异常"))
                    return
                }
                var attach = chatRoomMessageVo.getString("attach")

                var ext = chatRoomMessageVo.getMap("ext")

                var jsonObjExtAttachment = JSONObjExtAttachment()
                jsonObjExtAttachment.setMsgType(msgType!!)
                jsonObjExtAttachment.jSONObject = ext?.run {
                    var toHashMap = this.toHashMap()
                    JSONObject(toHashMap)
                } ?: JSONObject()


                var chatRoomMessage = MessageBuild.createChatRoomMessage(roomId!!, attach
                        ?: "", jsonObjExtAttachment)

                var server = IMClient.getIMServer(ChatRoomService::class.java)
                server.sendMessage(chatRoomMessage, object : RequestCallback<Void> {
                    override fun onSuccess(param: Void?) {
                        emitter.onNext(true)
                        emitter.onComplete()
                    }

                    override fun onFailed(code: Int) {
                        emitter.onNext(false)
                        emitter.onComplete()
                    }

                    override fun onException(exception: Throwable?) {
                        emitter.onError(exception ?: IOException())
                    }
                })
            }

        })

    }

    /**
     * 注册 单聊消息的监听
     *
     * @param observe 是否注册
     */
    fun registerReceiveP2PMessageObserve(observe: Boolean): Observable<Boolean> {

        var observer = object : Observer<List<P2PMessage>> {
            override fun onEvent(t: List<P2PMessage>) {

                var eventName = "observeReceiveP2PMessage"

                Ydk.getEventEmitter().emit(eventName, t)
            }
        }
        IMClient.getIMObserver(MessageObserver::class.java).observeReceiveMessage("YdkMqtt", observer, observe)

        return Observable.just(true)

    }

    /**
     *
     * 注册聊天室消息的监听
     *
     * @param observe 是否注册
     * @param roomId 聊天室ID
     *
     */
    fun registerReceiveChatRoomMessageObserve(observe: Boolean, roomId: String): Observable<Boolean> {

        var observer = object : Observer<List<ChatRoomMessage>> {
            override fun onEvent(t: List<ChatRoomMessage>) {

                var eventName = "${"observeReceiveChatRoomMessage"}$roomId"

                Ydk.getEventEmitter().emit(eventName, t)

            }
        }
        IMClient.getIMObserver(ChatRoomServiceObserver::class.java).observeReceiveMessage(roomId, observer, observe)

        return Observable.just(true)

    }

}