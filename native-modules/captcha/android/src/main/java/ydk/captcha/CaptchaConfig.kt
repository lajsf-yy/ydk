package ydk.captcha

import ydk.annotations.YdkConfigNode
import ydk.annotations.YdkConfigValue

@YdkConfigNode
data class CaptchaConfig(
        @YdkConfigValue(name = "httpBaseUrl")
        val httpBaseUrl: String,
        @YdkConfigValue(name = "apiVersion")
        val apiVersion: String
)