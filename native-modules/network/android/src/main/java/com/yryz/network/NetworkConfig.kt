package com.yryz.network

import ydk.annotations.YdkConfigNode
import ydk.annotations.YdkConfigValue

@YdkConfigNode
data class NetworkConfig(
        @YdkConfigValue(name = "appVersion")
        val appVersion: String,
        @YdkConfigValue(name = "name")
        val name: String,
        @YdkConfigValue(name = "apiVersion")
        val apiVersion: String,
        @YdkConfigValue(name = "socket.host")
        val socketHost: String,
        @YdkConfigValue(name = "socket.port")
        val socketPort: String,
        @YdkConfigValue(name = "httpBaseUrl")
        val httpBaseUrl: String,
        @YdkConfigValue(name = "webBaseUrl")
        val webBaseUrl: String,
        @YdkConfigValue(name = "oss.accessKeyId")
        val accessKeyId: String,
        @YdkConfigValue(name = "oss.secretAccessKey")
        val secretAccessKey: String,
        @YdkConfigValue(name = "oss.bucketName")
        val bucketName: String,
        @YdkConfigValue(name = "oss.cdn")
        val cdn: String
)