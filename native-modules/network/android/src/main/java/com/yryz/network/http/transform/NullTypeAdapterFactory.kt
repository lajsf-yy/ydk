package com.yryz.network.http.transform

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

class NullTypeAdapterFactory : TypeAdapterFactory {

    override fun <T : Any?> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {

        var rawType = type.rawType

        return when (rawType) {

            String::class.java -> {

                StringTypeAdapter() as TypeAdapter<T>
            }

            Long::class.java -> {

                LongTypeAdapter() as TypeAdapter<T>
            }

            Int::class.java, Short::class.java -> {

                IntTypeAdapter() as TypeAdapter<T>
            }

            Double::class.java, Float::class.java -> {

                DoubleTypeAdapter() as TypeAdapter<T>
            }

            Boolean::class.java -> {

                BooleanTypeAdapter() as TypeAdapter<T>
            }

            MyObject::class.java -> {
                MyObjectTypeAdapter(gson) as TypeAdapter<T>
            }
            else -> {
                null
            }
        }
    }
}


class StringTypeAdapter : TypeAdapter<String>() {
    override fun write(writer: JsonWriter?, value: String?) {
        if (value == null) {
            writer?.nullValue()
            return
        }
        writer?.value(value!!)
    }

    override fun read(reader: JsonReader?): String {
        if (reader?.peek() == JsonToken.NULL) {
            reader?.nextNull()
            return ""
        }

        return reader?.nextString() ?: ""
    }
}

class LongTypeAdapter : TypeAdapter<Long>() {
    override fun write(writer: JsonWriter?, value: Long?) {
        if (value == null) {
            writer?.nullValue()
            return
        }
        writer?.value(value!!)
    }

    override fun read(reader: JsonReader?): Long {
        if (reader?.peek() == JsonToken.NULL) {
            reader?.nextNull()
            return 0
        }

        return reader?.nextLong() ?: 0
    }
}

class IntTypeAdapter : TypeAdapter<Int>() {
    override fun write(writer: JsonWriter?, value: Int?) {
        if (value == null) {
            writer?.nullValue()
            return
        }
        writer?.value(value!!)
    }

    override fun read(reader: JsonReader?): Int {
        if (reader?.peek() == JsonToken.NULL) {
            reader?.nextNull()
            return 0
        }

        return reader?.nextInt() ?: 0
    }
}

class DoubleTypeAdapter : TypeAdapter<Double>() {
    override fun write(writer: JsonWriter?, value: Double?) {
        if (value == null) {
            writer?.nullValue()
            return
        }
        writer?.value(value!!)
    }

    override fun read(reader: JsonReader?): Double {
        if (reader?.peek() == JsonToken.NULL) {
            reader?.nextNull()
            return 0.0
        }

        return reader?.nextDouble() ?: 0.0
    }
}


class BooleanTypeAdapter : TypeAdapter<Boolean>() {
    override fun write(writer: JsonWriter?, value: Boolean?) {
        if (value == null) {
            writer?.nullValue();
            return
        }
        writer?.value(value!!)
    }

    override fun read(reader: JsonReader?): Boolean {
        if (reader?.peek() == JsonToken.NULL) {
            reader?.nextNull()
            return false
        }

        return reader?.nextBoolean() ?: false
    }
}

