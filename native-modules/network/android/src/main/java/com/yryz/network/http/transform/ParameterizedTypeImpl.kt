package com.yryz.network.http.transform

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class ParameterizedTypeImpl(var raw: Class<*>, var args: Array<Type>) : ParameterizedType {


    override fun getActualTypeArguments(): Array<Type> {
        return args
    }

    override fun getRawType(): Type {
        return raw
    }

    override fun getOwnerType(): Type? {
        return null
    }
}