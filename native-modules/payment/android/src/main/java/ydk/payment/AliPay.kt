package ydk.payment

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.alipay.sdk.app.PayTask
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Gsm on 2018/5/30.
 */
class AliPay {
    fun pay(activity: Activity, orderInfo: String): Observable<String> {
        return Observable.create { emitter: ObservableEmitter<String> ->
            var payTask = PayTask(activity)
            var result = payTask.pay(orderInfo, true)
            var aliPayResult = AliPayResult(result)
            if (TextUtils.equals("9000", aliPayResult.getResultStatus())) {
                emitter.onNext("支付成功")
                emitter.onComplete()
            } else if (TextUtils.equals("4000", aliPayResult.getResultStatus())
                    && isAliAppNotInstalled(activity)) {
                emitter.onNext("4000")
                emitter.onComplete()
            } else {
                emitter.onError(Exception(aliPayResult.getTips()))
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    fun isAliAppNotInstalled(activity: Activity): Boolean {
        var uri = Uri.parse("alipays://platformapi/startApp")
        var intent = Intent(Intent.ACTION_VIEW, uri)
        var name = intent.resolveActivity(activity.packageManager)
        return null == name
    }


}