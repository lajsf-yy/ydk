package com.laj.nutritionplan.wxapi;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import ydk.core.YdkConfigManager;
import ydk.payment.PayConfig;
import ydk.payment.R;
import ydk.payment.WeChatPay;
import ydk.share.wechat.WeChatLogin;
import ydk.share.wechat.WeChatShare;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {

    private static final String TAG = "WXEntryActivity";

    private IWXAPI api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pay_result);
        PayConfig config = YdkConfigManager.getConfig(PayConfig.class);
        api = WXAPIFactory.createWXAPI(this, config.getWechatAppId());
        api.handleIntent(getIntent(), this);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        api.handleIntent(intent, this);
    }

    @Override
    public void onReq(BaseReq req) {
        int type = req != null ? req.getType() : -99;
        Log.d(TAG, "onReq ### type=" + type);
    }

    /**
     * 0	成功	展示成功页面
     * -1	错误	可能的原因：签名错误、未注册APPID、项目设置APPID不正确、注册的APPID与设置的不匹配、其他异常等。
     * -2	用户取消	无需处理。发生场景：用户不支付了，点击取消，返回APP。
     *
     * @param resp
     */
    @Override
    public void onResp(BaseResp resp) {
        Log.d(TAG, "onResp ### type=" + resp.getType());
        Log.d(TAG, "onResp ### errCode=" + resp.errCode);
        switch (resp.getType()) {
            //微信登录
            case ConstantsAPI.COMMAND_SENDAUTH:
                WeChatLogin.Companion.resp(resp);
                break;
            //微信支付
            case ConstantsAPI.COMMAND_PAY_BY_WX:
                WeChatPay.Companion.resp(resp);
                break;
            //微信分享
            case ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX:
                WeChatShare.Companion.resp(resp);
                break;
        }
        finish();
    }
}