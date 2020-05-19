package com.yryz.network.http.token

import android.os.Build

class HttpHeader {

    var tenantId: String? = ""

    var devType: String? = ""

    var appVersion: String? = ""

    var userId: String? = ""

    var token: String? = ""

    var refreshToken: String? = ""

    var ditchCode: String? = ""

    var devId: String? = ""

    var devName:String? = ""

    var clientVersion:String?=""

    var ip: String? = ""

    constructor() {
        devName = (Build.MANUFACTURER + "-" + Build.MODEL + "-" + Build.BRAND + "-" + Build.DEVICE)
        clientVersion = (Build.MODEL + "-" + Build.VERSION.RELEASE)
        devId = DeviceUUID.loadDeviceId()
    }

    constructor(token: String?, refreshToken: String?) {
        this.token = token
        this.refreshToken = refreshToken
    }


    override fun toString(): String {
        return "HttpHeader(tenantId=$tenantId, devType=$devType, appVersion=$appVersion, userId=$userId, token=$token, refreshToken=$refreshToken, ditchCode=$ditchCode, devId=$devId, ip=$ip)"
    }


}

class RefreshToken {

    constructor(code: Int) {
        this.code = code
    }

    constructor(code: Int, refreshToken: String, token: String) {
        this.code = code
        this.refreshToken = refreshToken
        this.token = token
    }

    var code: Int? = 0

    var refreshToken: String? = ""

    var token: String? = ""
}
