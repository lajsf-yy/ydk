package com.yryz.network.http.transform

import com.yryz.network.http.EventBusTokenLose
import com.yryz.network.http.token.TokenIllegalStateException
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus


class ErrorConsumer(private var sendToRN: Boolean = false) : Consumer<Throwable> {
    override fun accept(throwable: Throwable) {
        //发送消息给RN
        if (throwable is TokenIllegalStateException) {
            Observable.just("finish")
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        //发通知
                        EventBus.getDefault().post(EventBusTokenLose())
                    }
        }
    }
}