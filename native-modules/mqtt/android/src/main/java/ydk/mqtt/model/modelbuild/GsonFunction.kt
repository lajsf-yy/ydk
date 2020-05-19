package ydk.mqtt.model.modelbuild

import com.google.gson.Gson

object GsonFunction {

    private val gson = Gson()

    /**
     * gson 转化器
     */
    fun <T> fromJson(json: String, classOfT: Class<T>): T {

        return gson.fromJson(json, classOfT)
    }

}