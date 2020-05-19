package com.yryz.network.http.token

import com.yryz.network.http.HttpClient
import com.yryz.network.http.transform.ErrorConsumer
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers

import io.reactivex.schedulers.Schedulers

class TokenController {

    companion object {

        /**
         * 提供给TCP 的刷新token 的方法
         * token 处理结果，返回true，短token处理成功，false,表示发送消息给RN去处理
         *
         */
        @Synchronized
        fun handlerToken(code: Int): Observable<Boolean> {
            when (code) {
                //短token过期,token无效
                TokenEnum.CODE_101.code, TokenEnum.CODE_102.code -> {
                    return asyncRenewToken()
                }
            }
            return Observable.just(false)
        }

        /**
         * 异步刷新token
         */
        private fun asyncRenewToken(): Observable<Boolean> {

            return Observable.just("token")
                    .flatMap { Observable.just(renewToken()) }
                    .flatMap { Observable.just(true) }
                    .doOnError(ErrorConsumer())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())

        }

        fun rnRenewToken(call: ((Int, HttpHeader?) -> Unit)) {

            Observable.just("token")
                    .flatMap { Observable.just(renewToken()) }
                    .map { TokenCache.getHttpHeader() }
                    //错误码回调回去
                    .doOnError(ErrorConsumer())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        call(TokenEnum.CODE_200.code, it)
                    }, {
                        when (it) {
                            is TokenIllegalStateException -> {
                                call(it.code.toInt(), null)
                            }
                        }
                    })
        }


        /**
         * 这是一个同步方法
         *
         * 该方法只允许在子线程调用
         */
        @Synchronized
        fun renewToken() {

            var baseModule = HttpClient.getClient().renewToken()

            return baseModule?.run {

                if (code.toInt() == TokenEnum.CODE_200.code && data != null) {

                    TokenCache.refreshToken(baseModule.data)

                    return
                }

                throw TokenIllegalStateException(baseModule.msg, code, baseModule)

            } ?: kotlin.run {

                throw TokenIllegalStateException("刷新token失败", TokenEnum.CODE_103.code.toString())

            }
        }

    }


}