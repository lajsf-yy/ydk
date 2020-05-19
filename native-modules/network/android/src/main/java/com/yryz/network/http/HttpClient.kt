package com.yryz.network.http

import com.yryz.network.NetworkConfig
import com.yryz.network.http.config.DefHttpConfiguration
import com.yryz.network.http.config.HttpConfiguration
import com.yryz.network.http.retrofit.RetrofitManage
import io.reactivex.Observable
import okhttp3.ResponseBody
import ydk.core.YdkConfigManager


object HttpClient {
    /**
     * 接口版本
     */
    const val URL_VERSIONS = "v1.3"

    private var version = ""

    var httpConfiguration: HttpConfiguration = DefHttpConfiguration()

    /**
     * 设置 httpConfiguration
     * @param httpConfiguration
     */
    fun initHttpClient(httpConfiguration: HttpConfiguration = DefHttpConfiguration()) {

        HttpClient.httpConfiguration = httpConfiguration
    }

    /**
     * 获取 url 版本号
     */
    fun getURLVersion(): String {

        if (version.isEmpty()) {
            val networkConfig = YdkConfigManager.getConfig(NetworkConfig::class.java)
            version = networkConfig.appVersion
        }

        return if (version.isEmpty()) URL_VERSIONS else version
    }

    /**
     * @param url 请求url
     * @param queryMap 请求体 表单
     * @return Observable<ResponseBody>
     */
    fun get(url: String, queryMap: Map<String, String> = emptyMap()): Observable<ResponseBody> {
        return RetrofitManage.instance.get(url, queryMap)
    }

    /**
     * @param url 请求url
     * @param fieldMap 请求体 表单
     * @return Observable<ResponseBody>
     */
    fun post(url: String, fieldMap: Map<String, String> = emptyMap()): Observable<ResponseBody> {
        return RetrofitManage.instance.post(url, fieldMap)
    }

    /**
     * @param url 请求url
     * @param json 请求体 body
     * @return Observable<ResponseBody>
     */
    fun post(url: String, json: String): Observable<ResponseBody> {
        return RetrofitManage.instance.post(url, json)
    }

    /**
     * delete  请求
     * @param url 请求url
     * @param json 请求体 body
     * @return Observable<ResponseBody>
     */
    fun delete(url: String, json: String = ""): Observable<ResponseBody> {
        return RetrofitManage.instance.delete(url, json)
    }

    /**
     * @return RetrofitManage
     */
    fun getClient(): RetrofitManage {
        return RetrofitManage.instance
    }

}