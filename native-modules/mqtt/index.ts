import { NativeModules, DeviceEventEmitter } from 'react-native'
/**
 * 消息的父类
 */
export class IMMessage {
  /**
   * 客户端消息id，使用uuid等随机串，msgId相同的消息会被客户端去重
   */
  msgId?: string
  /**
   * 消息发出者的账号uid
   */
  fromUid?: number
  /**
   * 消息类型
   * 0: 文本消息
   * N: 自定义消息类型
   */
  msgType?: string
  /**
   * 消息正文
   */
  attach?: string
  /**
   * 消息扩展字段
   */
  ext?: object
}
/**
 * 聊天室消息
 */
export class ChatRoomMessage extends IMMessage {
  /**
   * 聊天室id
   */
  roomId?: string
  /**
   * 消息发出者昵称
   */
  fromUname?: string
}
/**
 * P2P 消息
 */
export class P2PMessage extends IMMessage {
  /**
   * 消息接收者的账号uid
   */
  toUid?: number
}

/**
 * 消息发送的实体
 */
export class IMMessageVo {
  /**
   * 消息类型
   */
  msgType: string
  /**
   * 消息文本
   */
  attach?: string
  /**
   * 附件消息 json
   */
  ext?: object
}

export class ChatRoomMessageVo extends IMMessageVo {
  /**
   * 聊天室id
   */
  roomId: string
}
/**
 * P2P 消息
 */
export class P2PMessageVo extends IMMessageVo {
  /**
   * 消息接收者的账号uid
   */
  toUid: number
}

const YdkMqttModule = NativeModules.YdkMqttModule

const listeners = {}

const observeReceiveP2PMessage = 'observeReceiveP2PMessage'

const observeReceiveChatRoomMessage = 'observeReceiveChatRoomMessage'

export default class Mqtt {
  /**
   * mqtt 链接
   */
  connect(): Promise<void> {
    return YdkMqttModule.connect()
  }

  /**
   * mqtt 断开链接
   */
  disconnect(): Promise<void> {
    return YdkMqttModule.disconnect()
  }
  /**
   * 订阅chat room
   * @param subscribe 是否订阅
   * @param roomId 聊天室的ID
   */
  subscribeChatRoom(subscribe: boolean, roomId: string): Promise<void> {
    return YdkMqttModule.subscribeChatRoom(subscribe, roomId)
  }
  /**
   *发送 p2p 消息
   * @param p2PMessageVo 消息实体
   */
  sendP2PMessage(p2PMessageVo: P2PMessageVo): Promise<void> {
    return YdkMqttModule.sendP2PMessage(p2PMessageVo)
  }
  /**
   *发送 聊天室消息
   * @param chatRoomMessageVo  消息实体
   */
  sendChatRoomMessage(chatRoomMessageVo: ChatRoomMessageVo): Promise<void> {
    return YdkMqttModule.sendChatRoomMessage(chatRoomMessageVo)
  }

  /**
   *
   * @param observe 注册P2P消息
   * @param callback
   */
  observeReceiveP2PMessage(observe: boolean, callback: (data: P2PMessage[]) => void) {
    //通知原生模块，RN需要接收个人消息
    YdkMqttModule.registerReceiveP2PMessageObserve(observe)
    if (observe) {
      listeners[observeReceiveP2PMessage] = DeviceEventEmitter.addListener(
        observeReceiveP2PMessage,
        message => {
          callback(message)
        },
      )
    } else {
      listeners[observeReceiveP2PMessage] && listeners[observeReceiveP2PMessage].remove()
      listeners[observeReceiveP2PMessage] = null
    }
  }
  /**
   *注册 chatRoom 消息
   * @param observe 注册，取消注册
   * @param roomId 聊天室ID
   * @param callback 回调
   */
  observeReceiveChatRoomMessage(
    observe: boolean,
    roomId: string,
    callback: (data: ChatRoomMessage[]) => void,
  ) {
    //群组消息 type
    let type = `${observeReceiveChatRoomMessage}${roomId}`
    //通知原生模块，RN需要接收群组消息
    YdkMqttModule.registerReceiveChatRoomMessageObserve(observe, roomId)
    if (observe) {
      listeners[type] = DeviceEventEmitter.addListener(observeReceiveChatRoomMessage, message => {
        callback(message)
      })
    } else {
      listeners[type] && listeners[type].remove()
      listeners[type] = null
    }
  }
}
