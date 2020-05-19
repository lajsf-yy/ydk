package ydk.share.wechat

import com.tencent.mm.opensdk.modelbase.BaseResp
import io.reactivex.ObservableEmitter

class WeChatShare {

    companion object {

        private var shareEmitter: ObservableEmitter<MutableMap<String, String>>? = null

        fun setObservableEmitter(observableEmitter: ObservableEmitter<MutableMap<String, String>>) {
            shareEmitter = observableEmitter
        }

        fun resp(baseResp: BaseResp) {
            when (baseResp.errCode) {
                BaseResp.ErrCode.ERR_COMM -> shareEmitter?.onError(Exception("微信分享失败"))
                BaseResp.ErrCode.ERR_USER_CANCEL -> shareEmitter?.onError(Exception("微信分享取消"))
                BaseResp.ErrCode.ERR_AUTH_DENIED -> shareEmitter?.onError(Exception("微信分享错误"))
                BaseResp.ErrCode.ERR_OK -> {

                    shareEmitter?.onNext(mutableMapOf())
                    shareEmitter?.onComplete()
                }
            }
        }
    }
}