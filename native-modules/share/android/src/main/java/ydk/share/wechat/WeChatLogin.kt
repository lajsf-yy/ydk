package ydk.share.wechat

import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import com.tencent.mm.opensdk.modelmsg.SendAuth
import ydk.react.error.ResultException


class WeChatLogin {

    fun authorize(wxApi: IWXAPI): Observable<MutableMap<String, String>> {
        return Observable.create { emitter: ObservableEmitter<MutableMap<String, String>> ->
            if (!wxApi.isWXAppInstalled) {
                emitter.onError(ResultException("500", "没有安装微信"))
//            } else if (!wxApi.isWXAppSupportAPI) {
//                emitter.onError(ResultException("500", "当前微信版本不支持支付功能"))
            } else {
                loginEmitter = emitter
                val req = SendAuth.Req()
                req.scope = "snsapi_userinfo"
                req.state = "carjob_wx_login"
                wxApi.sendReq(req)
            }
        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    companion object {
        var loginEmitter: ObservableEmitter<MutableMap<String, String>>? = null
        fun resp(baseResp: BaseResp) {
            if (loginEmitter?.isDisposed != false) {
                return
            }
            when (baseResp.errCode) {
                BaseResp.ErrCode.ERR_COMM -> loginEmitter?.onError(Exception("微信登录失败"))
                BaseResp.ErrCode.ERR_USER_CANCEL -> loginEmitter?.onError(Exception("微信登录取消"))
                BaseResp.ErrCode.ERR_AUTH_DENIED -> loginEmitter?.onError(Exception("微信登录错误"))
                BaseResp.ErrCode.ERR_OK -> {
                    val resp = baseResp as SendAuth.Resp
                    val code = resp.code
                    var mutableMapOf = mutableMapOf<String, String>()
                    mutableMapOf["code"] = code
                    loginEmitter?.onNext(mutableMapOf)
                    loginEmitter?.onComplete()
                }
            }
        }
    }
}