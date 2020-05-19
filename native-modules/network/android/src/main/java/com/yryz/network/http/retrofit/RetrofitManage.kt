package com.yryz.network.http.retrofit

import android.text.TextUtils
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.yryz.network.BuildConfig
import com.yryz.network.NetworkConfig
import com.yryz.network.http.HttpClient
import com.yryz.network.http.model.AuthTokenVO
import com.yryz.network.http.model.BaseModel
import com.yryz.network.http.model.RefreshTokenVo
import com.yryz.network.http.transform.ErrorConsumer
import com.yryz.network.http.transform.NullTypeAdapterFactory
import com.yryz.network.http.token.TokenCache
import com.yryz.network.http.token.TokenIllegalStateException
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import okhttp3.*
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import ydk.core.YdkConfigManager
import java.util.concurrent.TimeUnit


class RetrofitManage private constructor() {

    private val networkConfig: NetworkConfig = YdkConfigManager.getConfig(NetworkConfig::class.java)

    private lateinit var mRetrofitService: RetrofitService

    private lateinit var mRetrofitService2: RetrofitService

    private lateinit var mRetrofit: Retrofit

    private var DEFAULT_C_TIMEOUT = 15_000L
    private var DEFAULT_W_TIMEOUT = 15_000L
    private var DEFAULT_R_TIMEOUT = 15_000L


    private var gson: Gson = GsonBuilder()
            .registerTypeAdapterFactory(NullTypeAdapterFactory())
            .create()

    init {
        HttpClient.httpConfiguration?.apply {
            DEFAULT_C_TIMEOUT = this.connectTimeout()
            DEFAULT_W_TIMEOUT = this.writeTimeout()
            DEFAULT_R_TIMEOUT = this.readTimeout()
        }
        createRetrofit()
        createRetrofit2()
    }

    companion object {

        val instance: RetrofitManage by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            RetrofitManage()
        }
    }

    /**
     * 获取baseurl
     */
    private fun baseUrl(): String {

        return if (networkConfig.httpBaseUrl.endsWith("/")) networkConfig.httpBaseUrl else "${networkConfig.httpBaseUrl}/"

    }

    /**
     * 创建 Retrofit
     * 具有拦截器，会处理公共的token以及刷新token
     */
    private fun createRetrofit() {

        val builder = OkHttpClient.Builder()

        builder.addInterceptor(HeaderInterceptor())
                .connectTimeout(DEFAULT_C_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(DEFAULT_W_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_R_TIMEOUT, TimeUnit.MILLISECONDS)
                .dns(ApiDns())

        if (BuildConfig.DEBUG) {
            // Log信息拦截器
            val loggingInterceptor = HttpLoggingInterceptor()
            //这里可以选择拦截级别
            loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

            //设置 Debug Log 模式
            builder.addInterceptor(loggingInterceptor)
        }

        mRetrofit = Retrofit.Builder()
                .baseUrl(baseUrl())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(builder.build())
                .build()
        mRetrofitService = mRetrofit.create(RetrofitService::class.java)
    }

    /**
     * 创建没有拦截器的 会添加公共token ,不具备自动刷新token的能力
     */
    private fun createRetrofit2() {

        val builder = OkHttpClient.Builder()

        builder.addInterceptor(HeaderInterceptor2())
                .connectTimeout(DEFAULT_C_TIMEOUT, TimeUnit.MILLISECONDS)
                .writeTimeout(DEFAULT_W_TIMEOUT, TimeUnit.MILLISECONDS)
                .readTimeout(DEFAULT_R_TIMEOUT, TimeUnit.MILLISECONDS)

        var retrofit = Retrofit.Builder()
                .baseUrl(baseUrl())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build()

        mRetrofitService2 = retrofit.create(RetrofitService::class.java)
    }

    /**
     * 创建自定义service
     */
    fun <T> createService(service: Class<T>): T {

        return mRetrofit.create(service)
    }


    private fun applySchedulers(): ObservableTransformer<ResponseBody, ResponseBody> {

        return ObservableTransformer { upstream ->
            upstream.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
        }
    }

    /**
     * 拼接完整的url
     */
    private fun url(url: String): String {

        if (url.startsWith(networkConfig.httpBaseUrl)) {
            return url
        }
        return networkConfig.httpBaseUrl + url
    }

    /**
     * get 接口
     */
    fun get(url: String, queryMap: Map<String, String> = emptyMap()): Observable<ResponseBody> {

        if (queryMap.isEmpty()) {
            return mRetrofitService.get(url(url))
                    .doOnError(ErrorConsumer())
        }
        return mRetrofitService.get(url(url), queryMap)
                .doOnError(ErrorConsumer())
    }

    /**
     * post 表单接口
     */
    fun post(url: String, fieldMap: Map<String, String> = emptyMap()): Observable<ResponseBody> {

        if (fieldMap.isEmpty()) {
            return mRetrofitService.post(url(url))
                    .doOnError(ErrorConsumer())
        }

        return mRetrofitService.post(url(url), fieldMap)
                .doOnError(ErrorConsumer())
    }

    /**
     * post RequestBody
     */
    fun post(url: String, json: String): Observable<ResponseBody> {

        var body = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), json)

        return mRetrofitService.post(url(url), body)
                .doOnError(ErrorConsumer())
    }


    /**
     * post 表单接口
     */
    fun put(url: String, fieldMap: Map<String, String> = emptyMap()): Observable<ResponseBody> {

        if (fieldMap.isEmpty()) {
            return mRetrofitService.put(url(url))
                    .doOnError(ErrorConsumer())
        }

        return mRetrofitService.put(url(url), fieldMap)
                .doOnError(ErrorConsumer())
    }

    /**
     * post RequestBody
     */
    fun put(url: String, json: String): Observable<ResponseBody> {

        var body = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), json)

        return mRetrofitService.put(url(url), body)
                .doOnError(ErrorConsumer())
    }

    /**
     * delete 接口
     *
     */
    fun delete(url: String, json: String = ""): Observable<ResponseBody> {

        if (TextUtils.isEmpty(json)) {
            return mRetrofitService.delete(url(url))
                    .doOnError(ErrorConsumer())
        }
        var body = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), json)

        return mRetrofitService.delete(url(url), body)
                .doOnError(ErrorConsumer())
    }

    /**
     * 同步刷新 token
     */
    fun renewToken(): BaseModel<RefreshTokenVo>? {

        var httpHeader = TokenCache.getHttpHeader()

        if (TextUtils.isEmpty(httpHeader.refreshToken) ||
                TextUtils.isEmpty(httpHeader.userId)) {
            throw TokenIllegalStateException("您的登录信息过期，请重新登录。", "101")
        }

        var authTokenVO = AuthTokenVO(httpHeader.refreshToken!!, httpHeader.token!!)

        var url = "/platform-user/${networkConfig.apiVersion}/pb/auth/action/refreshToken"

        var call = mRetrofitService2.renewToken(url(url), authTokenVO)

        var execute = call.execute()

        return execute.body()

    }

    fun getRetrofitService(): RetrofitService = mRetrofitService

    fun getRetrofitService2(): RetrofitService = mRetrofitService2
}



