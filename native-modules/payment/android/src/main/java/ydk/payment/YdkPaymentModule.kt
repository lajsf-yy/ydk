package ydk.payment

import com.facebook.react.bridge.*
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import ydk.core.YdkConfigManager


/**
 * Created by Gsm on 2018/6/25.
 */
class YdkPaymentModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    private val payConfig: PayConfig by lazy {
        YdkConfigManager.getConfig(PayConfig::class.java)
    }

    override fun getName(): String {
        return "YdkPaymentModule"
    }

    @ReactMethod
    fun pay(map: ReadableMap, promise: Promise) {
        //payChannel 支付渠道：1支付宝，2微信，3苹果支付 number
        //ext 代签名的支付参数对象 支付实体
        when (map.getString("payChannel")) {
            "1" -> aliPay(map.getMap("ext")!!.toHashMap().get("orderStr").toString(), promise)
            "2" -> weChatPay(map.getMap("ext")!!.toHashMap(), promise)
            else -> promise.reject("500", "不支持的支付类型")
        }
    }

    private fun weChatPay(map: HashMap<String, Any>, promise: Promise) {
        var wechatAppId = payConfig.wechatAppId
        var wxapi = WXAPIFactory.createWXAPI(currentActivity, wechatAppId)
        wxapi.registerApp(wechatAppId)
        if (!wxapi.isWXAppInstalled) {
            val map = Arguments.createMap()
            map.putString("msg", "没有安装微信")
            promise.reject("500", "没有安装微信", map)
//        } else if (!wxapi.isWXAppSupportAPI) {
//            val map = Arguments.createMap()
//            map.putString("msg", "当前微信版本不支持支付功能")
//            promise.reject("500", "当前微信版本不支持支付功能", map)
        } else {
            WeChatPay().pay(wxapi, getWeChatInfo(map)).subscribe({ result: String ->
                promise.resolve(result)
            }, { error: Throwable ->
                promise.reject(error)
            })
        }
    }

    private fun getWeChatInfo(map: HashMap<String, Any>): PayReq {
        var payReq = PayReq()
        payReq.appId = map["appid"].toString()
        payReq.partnerId = map["partnerid"].toString()
        payReq.prepayId = map["prepayid"].toString()
        payReq.nonceStr = map["noncestr"].toString()
        payReq.timeStamp = map["timestamp"].toString()
        payReq.packageValue = map["package"].toString()
        payReq.sign = map["sign"].toString()
        return payReq
    }

    private fun aliPay(orderInfo: String, promise: Promise) {
        AliPay().pay(currentActivity!!, orderInfo).subscribe({ result: String ->
            promise.resolve(result)
        }, { error: Throwable ->
            promise.reject("500", error)
        })
    }
}