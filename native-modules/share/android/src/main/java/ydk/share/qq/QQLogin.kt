package ydk.share.qq

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.tencent.connect.common.Constants
import com.tencent.tauth.IUiListener
import com.tencent.tauth.Tencent
import com.tencent.tauth.UiError
import io.reactivex.Observable
import io.reactivex.ObservableEmitter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject
import ydk.react.error.ResultException

class QQLogin {

    companion object {

        private var uiListener: BaseUiListener? = null

        fun getQQLoginRequestCode(): Int = Constants.REQUEST_LOGIN

        fun onActivityResultData(requestCode: Int, resultCode: Int, data: Intent?) {
            if (requestCode == Constants.REQUEST_LOGIN && uiListener != null) {
                Tencent.onActivityResultData(requestCode, resultCode, data, uiListener);
            }
        }

        fun clearUiListener() {
            uiListener = null
        }
    }


    fun authorize(activity: Activity, tencentApi: Tencent): Observable<MutableMap<String, String>> {
        return Observable.create { emitter: ObservableEmitter<MutableMap<String, String>> ->
            if (!tencentApi.isQQInstalled(activity)) {
                emitter.onError(ResultException("500", "没有安装QQ"))
            } else {
                uiListener = BaseUiListener(emitter)
                // tencentApi.loginServerSide(activity, "", uiListener)
                tencentApi.login(activity, "", uiListener)
            }

        }.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
    }

    class BaseUiListener(private val emitter: ObservableEmitter<MutableMap<String, String>>) : IUiListener {

        override fun onComplete(any: Any?) {
            Log.e("QQLogin", "authorize onComplete")
            if (any == null) {
                emitter.onError(ResultException("500", "QQ登录错误"))
                return
            }
            var josn = any as JSONObject
            val accessToken = josn.getString("access_token")
            var mutableMapOf = mutableMapOf<String, String>()
            mutableMapOf["code"] = accessToken
            emitter.onNext(mutableMapOf)
            emitter.onComplete()
            clearUiListener()

        }

        override fun onCancel() {
            Log.e("QQLogin", "authorize onCancel")
            emitter.onError(ResultException("500", "QQ登录取消"))
            clearUiListener()
        }

        override fun onError(error: UiError?) {
            var errorCode = error?.errorCode
            var errorDetail = error?.errorDetail
            var errorMessage = error?.errorMessage
            Log.e("QQLogin", "authorize onError errorCode=$errorCode")
            Log.e("QQLogin", "authorize onError errorDetail=$errorDetail")
            Log.e("QQLogin", "authorize onError errorMessage=$errorMessage")
            emitter.onError(ResultException("500", "QQ登录错误"))
            clearUiListener()
        }
    }

}