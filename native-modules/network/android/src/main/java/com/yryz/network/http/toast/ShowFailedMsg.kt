package com.yryz.network.http.toast

import android.text.TextUtils
import android.view.Gravity
import android.widget.Toast
import ydk.core.activityresult.RxActivityResult

object ShowFailedMsg {

    @Volatile
    private var showTime = 0L

    fun showFailedMsg(message: String?) {
        if (TextUtils.isEmpty(message)) {
            return
        }
        if (System.currentTimeMillis() - showTime < 3000) {
            return
        }
        showTime = System.currentTimeMillis()
        RxActivityResult.getLiveActivity()?.runOnUiThread {
            RxActivityResult.getLiveActivity()?.run {
                var toast = Toast.makeText(this, message, Toast.LENGTH_LONG)
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show()
            }
        }
    }
}