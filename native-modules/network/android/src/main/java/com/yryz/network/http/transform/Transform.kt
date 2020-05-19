package com.yryz.network.http.transform

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.yryz.network.http.model.AuthTokenVO
import com.yryz.network.http.model.BaseModel


class Transform {

    companion object {

        //   private var mGson: Gson = Gson()

        private var mGson: Gson = GsonBuilder()
                .registerTypeAdapterFactory(NullTypeAdapterFactory())
                .create()

        fun transFormToBaseModel(json: String): BaseModel<Object> {

            return mGson.fromJson<BaseModel<Object>>(json, BaseModel::class.java)

        }

        fun <T> fromJsonObject(json: String, clazz: Class<T>): BaseModel<T> {
            val type = ParameterizedTypeImpl(BaseModel::class.java, arrayOf(clazz))
            return mGson.fromJson(json, type)
        }


        fun <T> fromJsonArray(json: String, clazz: Class<T>): BaseModel<List<T>> {
            // 生成List<T> 中的 List<T>
            val listType = ParameterizedTypeImpl(List::class.java, arrayOf(clazz))
            // 根据List<T>生成完整的Result<List<T>>
            val type = ParameterizedTypeImpl(BaseModel::class.java!!, arrayOf(listType))

            return mGson.fromJson(json, type)
        }


        fun transFormToAuthTokenVO(json: String): BaseModel<AuthTokenVO> {

            return mGson.fromJson(json, object : TypeToken<BaseModel<AuthTokenVO>>() {

            }.type)

        }

    }
}