package com.yryz.network.http.model

data class AuthTokenVO(var refreshToken: String, var token: String, var from: String = "native") {
}