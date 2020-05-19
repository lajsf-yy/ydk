package com.yryz.network.http

import com.yryz.network.http.model.RefreshTokenVo

/**
 * token 失效
 */
class EventBusTokenLose {

}

/**
 * token 刷新
 */
class EventBusRefreshToken(var refreshTokenVo: RefreshTokenVo) {

}