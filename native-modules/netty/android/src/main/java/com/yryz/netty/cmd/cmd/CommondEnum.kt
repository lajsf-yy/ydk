package com.yryz.netty.cmd.cmd

enum class CommondEnum(val value: Int) {

    NONE(0),
    /**
     * 登录
     */
    CMD_LOGIN_SERVER(1000),
    /**
     * 心跳
     */
    CMD_HEARTBEAT(1001),
    /**
     * 心跳回执
     */
    CMD_HEARTBEAT_RECEIPT(-1001),

    /**
     * 服务端授权结果
     */
    CMD_LOGIN_AUTHORIZATION(2000);


    companion object {

        fun typeOfValue(value: Int): CommondEnum {
            for (e in values()) {
                if (e.value === value) {
                    return e
                }
            }
            return NONE
        }
    }
}