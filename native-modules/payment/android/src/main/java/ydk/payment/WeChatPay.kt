package ydk.payment

import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * Created by Gsm on 2018/5/31.
 */
class WeChatPay {

    fun pay(wxApi: IWXAPI, payReq: PayReq): Observable<String> {
        return Observable.create { emitter: ObservableEmitter<String> ->
            wxApi.sendReq(payReq)
            payEmitter = emitter
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    companion object {
        var payEmitter: ObservableEmitter<String>? = null
        fun resp(baseResp: BaseResp) {
            when (baseResp.errCode) {
                BaseResp.ErrCode.ERR_COMM -> payEmitter!!.onError(Exception("支付失败"))
                BaseResp.ErrCode.ERR_USER_CANCEL -> payEmitter!!.onError(Exception("支付取消"))
                BaseResp.ErrCode.ERR_AUTH_DENIED -> payEmitter!!.onError(Exception("支付错误"))
                BaseResp.ErrCode.ERR_OK -> {
                    payEmitter?.onNext("支付成功")
                    payEmitter?.onComplete()
                }
            }
        }
    }

}