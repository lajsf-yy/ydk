package com.yryz.network.http.token

import android.text.TextUtils
import com.yryz.network.http.EventBusRefreshToken
import com.yryz.network.http.model.RefreshTokenVo
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class TokenCache {

    companion object {

        private val mHttpHeader = HttpHeader()

        @Synchronized
        fun refreshToken(httpHeader: HttpHeader?) {

            httpHeader?.apply {

                if (!TextUtils.isEmpty(appVersion)) {
                    TokenCache.mHttpHeader.appVersion = appVersion
                }
                if (!TextUtils.isEmpty(devId)) {
                    TokenCache.mHttpHeader.devId = devId
                }
                if (!TextUtils.isEmpty(devType)) {
                    TokenCache.mHttpHeader.devType = devType
                }
                if (!TextUtils.isEmpty(ditchCode)) {
                    TokenCache.mHttpHeader.ditchCode = ditchCode
                }
                if (!TextUtils.isEmpty(ip)) {
                    TokenCache.mHttpHeader.ip = ip
                }
                if (!TextUtils.isEmpty(refreshToken)) {
                    TokenCache.mHttpHeader.refreshToken = refreshToken
                }
                if (!TextUtils.isEmpty(tenantId)) {
                    TokenCache.mHttpHeader.tenantId = tenantId
                }
                if (!TextUtils.isEmpty(token)) {
                    TokenCache.mHttpHeader.token = token
                }
                if (!TextUtils.isEmpty(userId)) {
                    TokenCache.mHttpHeader.userId = userId
                }
            }

        }

        @Synchronized
        fun clearToken() {
            TokenCache.mHttpHeader.refreshToken = ""
            TokenCache.mHttpHeader.token = ""
            TokenCache.mHttpHeader.userId = ""
        }


        @Synchronized
        fun refreshToken(refreshTokenVo: RefreshTokenVo) {

            TokenCache.mHttpHeader.token = refreshTokenVo.token

            TokenCache.mHttpHeader.refreshToken = refreshTokenVo.refreshToken

            Observable.just(refreshTokenVo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        EventBus.getDefault().post(EventBusRefreshToken(it))
                    }, {})

        }

        fun getHttpHeader(): HttpHeader {

            return mHttpHeader
        }

    }
}