package com.yryz.network.http.retrofit

import android.text.TextUtils
import com.yryz.network.NetworkConfig
import com.yryz.network.http.HttpClient
import com.yryz.network.http.toast.ShowFailedMsg
import okhttp3.*
import okhttp3.ResponseBody
import okhttp3.RequestBody
import com.yryz.network.http.transform.Transform
import com.yryz.network.http.token.*
import ydk.core.YdkConfigManager


class HeaderInterceptor : Interceptor {

    private var apiVersion: String

    init {
        apiVersion = getVersion()
    }

    private fun getVersion(): String {
        val config = YdkConfigManager.getConfig(NetworkConfig::class.java) ?: return "v1.0"
        return config.apiVersion
    }

    /**
     * 添加公共的请求头
     *
     */
    private fun addPublicaHeader(build: Request.Builder) {

        var publicaHeader = HttpClient.httpConfiguration.publicaHeader

        for (entry in publicaHeader) {
            build.header(entry.key, entry.value)
        }

    }


    override fun intercept(chain: Interceptor.Chain): Response? {

        var request = chain.request()

        val url = request.url()

        var urlStr = url.toString()
        urlStr = urlStr.replace("[api_version]", apiVersion)
        val requestBuilder = request.newBuilder()
        //添加公共头
        addPublicaHeader(requestBuilder)

        var requestBody = request.body()

        var method = request.method()

        requestBuilder.url(urlStr)

        requestBuilder.method(request.method(), requestBody)

        request = requestBuilder.build()
        //发请求
        val response = chain.proceed(request)

        if (response == null) {
            return response
        }
        var httpCode = response.code()

        when (httpCode) {

            200, 401 -> {

                val responseBody = response.body()
                var mediaType = MediaType.parse("application/json;charset=UTF-8")
                if (responseBody == null) {
                    val requestBodyDec = ResponseBody.create(mediaType, "{}")
                    return response.newBuilder().body(requestBodyDec).build()
                }

                val responseBodyString = responseBody.string()

                if (TextUtils.isEmpty(responseBodyString)) {
                    val requestBodyDec = ResponseBody.create(mediaType, responseBodyString)
                    return response.newBuilder().body(requestBodyDec).build()
                }

                val baseModel = Transform.transFormToBaseModel(responseBodyString)
                var code = baseModel.code.toInt()
                var tokenEnum = TokenEnum.codeToValuOf(baseModel.code)

                when (tokenEnum) {
                    //短token过期,token无效
                    TokenEnum.CODE_101, TokenEnum.CODE_102 -> {
                        try {
                            TokenController.renewToken()
                        } catch (e: Exception) {
                            TokenCache.clearToken()
                            //  throw TokenIllegalStateException("长TOKEN失效", "1000")
                            throw TokenIllegalStateException("您的登录信息过期，请重新登录。", tokenEnum.code.toString())
                        }

                        return newRequest(chain, urlStr, method, requestBody)

                    }
                    //长token失效
                    TokenEnum.CODE_103, TokenEnum.CODE_104 -> {
                        TokenCache.clearToken()
                        throw TokenIllegalStateException(baseModel.msg, baseModel.code, baseModel)
                    }
                    else -> {
                        if (code < 1000 && code != TokenEnum.CODE_200.code) {
                            ShowFailedMsg.showFailedMsg(baseModel.msg)
                        }
                        val requestBodyDec = ResponseBody.create(mediaType, responseBodyString)
                        return response.newBuilder().body(requestBodyDec).build()
                    }
                }
            }
            else -> {

                return response
            }

        }
    }


    /**
     * 新的请求
     */
    private fun newRequest(chain: Interceptor.Chain, oldUrl: String, method: String, requestBody: RequestBody?): Response {

        val requestBuilder = Request.Builder()

        addPublicaHeader(requestBuilder)

        val request = requestBuilder.url(oldUrl).method(method, requestBody).build()

        return chain.proceed(request)
    }

}