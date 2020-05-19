package com.yryz.network.http.retrofit

import com.yryz.network.http.model.AuthTokenVO
import com.yryz.network.http.model.BaseModel
import com.yryz.network.http.model.RefreshTokenVo
import io.reactivex.Observable
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface RetrofitService {

//    companion object {
//        const val URL_REFRESHTOKEN = "platform-user/${HttpClient.URL_VERSIONS}/pb/auth/action/refreshToken"
//    }

    @POST
    fun renewToken(@Url url: String, @Body authTokenVO: AuthTokenVO): Call<BaseModel<RefreshTokenVo>>


    /**
     * RESTful 风格的接口
     */
    @GET
    fun get(@Url url: String): Observable<ResponseBody>

    @GET
    fun get(@Url url: String, @QueryMap queryMap: Map<String, String>): Observable<ResponseBody>


    /**
     * RESTful 风格的接口
     */
    @POST
    fun post(@Url url: String): Observable<ResponseBody>

    @FormUrlEncoded
    @POST
    fun post(@Url url: String, @FieldMap fieldMap: Map<String, String>): Observable<ResponseBody>

    @POST
    fun post(@Url url: String, @Body body: RequestBody): Observable<ResponseBody>


    /**
     * RESTful 风格的接口
     */
    @DELETE
    fun delete(@Url url: String): Observable<ResponseBody>


    @HTTP(method = "DELETE", hasBody = true)
    fun delete(@Url url: String, @Body body: RequestBody): Observable<ResponseBody>

    /**
     * RESTful 风格的接口
     */
    @POST
    fun put(@Url url: String): Observable<ResponseBody>

    @FormUrlEncoded
    @PUT
    fun put(@Url url: String, @FieldMap fieldMap: Map<String, String>): Observable<ResponseBody>

    @PUT
    fun put(@Url url: String, @Body bod: RequestBody): Observable<ResponseBody>
}