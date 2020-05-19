package com.yryz.network.http.retrofit

import com.yryz.network.http.HttpClient
import okhttp3.Interceptor
import okhttp3.Response


class HeaderInterceptor2 : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {


        var publicaHeader = HttpClient.httpConfiguration.publicaHeader
        var newBuilder = chain.request().newBuilder()
        for (entry in publicaHeader) {
            newBuilder.header(entry.key, entry.value)
        }
        var request =
                newBuilder.build()

        return chain.proceed(request)

    }


}