package ydk.share


import ydk.annotations.YdkConfigNode

@YdkConfigNode(name = "share")
class ShareConfig(
        val mobAppKey: String,
        val mobAppSecret: String,
        val sinaAppKey: String,
        val sinaAppSecret: String,
        val sinaRedirectUri: String,
        val wechatAppId: String,
        val wechatAppSecret: String,
        val wechatMiniProgramerId: String,
        val qqAppKey: String,
        val qqAppId: String) {

    fun getMobConfig(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        hashMap.put("mobAppKey", mobAppKey)
        hashMap.put("mobAppSecret", mobAppSecret)
        return addCommonConfig(hashMap)
    }

    fun getQQConfig(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        hashMap.put("AppId", qqAppId)
        hashMap.put("AppKey", qqAppKey)
        return addCommonConfig(hashMap)
    }


    fun getWeChatConfig(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        hashMap.put("AppId", wechatAppId)
        hashMap.put("AppSecret", wechatAppSecret)
        hashMap.put("userName", wechatMiniProgramerId)
        return addCommonConfig(hashMap)
    }


    fun getSinaConfig(): HashMap<String, Any> {
        val hashMap = HashMap<String, Any>()
        hashMap.put("AppKey", sinaAppKey)
        hashMap.put("AppSecret", sinaAppSecret)
        hashMap.put("RedirectUrl", sinaRedirectUri)
        return addCommonConfig(hashMap)
    }


    private fun addCommonConfig(hashMap: HashMap<String, Any>): HashMap<String, Any> {
        hashMap["ShareByAppClient"] = "true"//是否使用客户端进行分享
        hashMap["Enable"] = "true"//Enable字段表示此平台是否有效，布尔值，默认为true，如果Enable为false，即便平台的jar包已经添加到应用中，平台实例依然不可获取。
        return hashMap
    }
}