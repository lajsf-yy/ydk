package ydk.mqtt.model

enum class StatusCode(val value: Int) {
    /**
     * 未定义
     */
    INVALID(0),

    /**
     * 断开链接
     */
    DISCONNECT(1),

    /**
     * 已链接
     */
    CONNECT(2),

    /**
     * 被其他端的登录踢掉
     */
    KICKOUT(3),

    /**
     * 用户名或密码错误
     */
    PWD_ERROR(4),
    /**
     * 链接异常
     */
    CONNECT_ERROR(5),

    /**
     * 非法的鉴权信息 主要指长token 失效
     */
    NOT_AUTHORIZED(6);


}

class MqttStatus(var statusCode: StatusCode = StatusCode.DISCONNECT) {
    /**
     * 判断处于当前状态码时，SDK还会不会继续自动重连登录。
     *
     * @return 如果返回true，SDK将停止自动登录，需要上层app显示调用 mqtt 才能继续链接
     */
    fun wontAutoLogin(): Boolean {

        return this.statusCode == StatusCode.KICKOUT ||
                this.statusCode == StatusCode.PWD_ERROR ||
                this.statusCode == StatusCode.CONNECT_ERROR ||
                this.statusCode == StatusCode.PWD_ERROR ||
                this.statusCode == StatusCode.INVALID

    }


}