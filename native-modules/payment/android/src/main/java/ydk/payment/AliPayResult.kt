package ydk.payment

import android.text.TextUtils
import java.util.*

/**
 * Created by Gsm on 2018/5/30.
 */
class AliPayResult(rawResult: String) {

    private lateinit var resultStatus: String

    init {
        var resultParams = rawResult.split(";")
        for (param: String in resultParams) {
            if (param.startsWith("resultStatus"))
                resultStatus = getValue(param, "resultStatus")
        }
    }

    companion object {
        private var mResultStatus: HashMap<String, String> = HashMap()

        init {
            mResultStatus["9000"] = "操作成功！"
            mResultStatus["4000"] = "系统异常！"
            mResultStatus["4001"] = "数据格式不正确！"
            mResultStatus["4003"] = "该用户绑定的支付宝账户被冻结或不允许支付！"
            mResultStatus["4004"] = "该用户已解除绑定！"
            mResultStatus["4005"] = "绑定失败或没有绑定！"
            mResultStatus["4006"] = "订单支付失败！"
            mResultStatus["4010"] = "重新绑定账户！"
            mResultStatus["6000"] = "支付服务正在进行升级操作！"
            mResultStatus["6001"] = "用户中途取消支付操作！"
            mResultStatus["6002"] = "网络连接出错！"
            mResultStatus["7001"] = "网页支付失败！"
        }
    }

    private fun getValue(content: String, key: String): String {
        var perfix = "$key={"
        return content.substring(content.indexOf(perfix) + perfix.length, content.lastIndexOf("}"))
    }

    fun getTips(): String {
        if (TextUtils.isEmpty(resultStatus))
            resultStatus = getResultStatus()
        if (mResultStatus.containsKey(resultStatus)) {
            return mResultStatus[resultStatus].toString()
        }
        return "未知错误"
    }

    fun getResultStatus() = resultStatus

}