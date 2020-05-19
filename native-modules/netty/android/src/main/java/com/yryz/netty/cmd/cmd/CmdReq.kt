package com.yryz.netty.cmd.cmd


data class BaseCmdReq(var cmd: Int) {}

data class LoginCmdReq(
        var cmd: Int,
        var userId: Long,
        var token: String,
        var appVersion: String,
        var tenantId: String = "lovelorn",
        var devType: Int = 2) {

}

data class LoginCmdResult(
        var cmd: Int,
        var code: String,
        var msg: String
) {

}




