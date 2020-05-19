package com.yryz.network.http.transform

import com.yryz.network.http.model.BaseModel
import io.reactivex.functions.Function
import okhttp3.ResponseBody

/**
 * 转化器一
 */
class TransFormFunction1<T>(private var clazz: Class<T>) : Function<ResponseBody, BaseModel<T>> {

    override fun apply(responseBody: ResponseBody): BaseModel<T> {


        var clazzStr = when (clazz) {

            Boolean::class.java -> "java.lang.Boolean"
            Char::class.java -> "java.lang.Character"
            Byte::class.java -> "java.lang.Byte"
            Short::class.java -> "java.lang.Short"
            Int::class.java -> "java.lang.Integer"
            Float::class.java -> "java.lang.Float"
            Long::class.java -> "java.lang.Long"
            Double::class.java -> "java.lang.Double"
            Void.TYPE -> "java.lang.Void"
            else -> {
                null
            }
        }


        if (clazzStr != null) {
            clazz = Class.forName(clazzStr) as Class<T>
        }

        val string = responseBody.string()

        return Transform.fromJsonObject(string, clazz)
    }

}

/**
 * 转化器二
 */
class TransFormFunction2<T>(private var clazz: Class<T>) : Function<ResponseBody, BaseModel<List<T>>> {

    override fun apply(responseBody: ResponseBody): BaseModel<List<T>> {

        val string = responseBody.string()

        var clazzStr = when (clazz) {

            Boolean::class.java -> "java.lang.Boolean"
            Char::class.java -> "java.lang.Character"
            Byte::class.java -> "java.lang.Byte"
            Short::class.java -> "java.lang.Short"
            Int::class.java -> "java.lang.Integer"
            Float::class.java -> "java.lang.Float"
            Long::class.java -> "java.lang.Long"
            Double::class.java -> "java.lang.Double"
            Void.TYPE -> "java.lang.Void"
            else -> {
                null
            }
        }

        if (clazzStr != null) {
            clazz = Class.forName(clazzStr) as Class<T>
        }

        return Transform.fromJsonArray(string, clazz)


    }
}



