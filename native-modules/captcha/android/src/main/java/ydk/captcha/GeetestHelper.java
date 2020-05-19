package ydk.captcha;

import android.content.Context;

import com.geetest.sdk.Bind.GT3GeetestUtilsBind;

public class GeetestHelper {
    private static final GeetestHelper ourInstance = new GeetestHelper();

    public static GeetestHelper getInstance() {
        return ourInstance;
    }

    public final static String OnCaptchaSuccess = "OnCaptchaSuccess";//'成功'
    public final static String OnCaptchaCancel = "OnCaptchaCancel";//'取消'
    public final static String OnCaptchaError = "OnCaptchaError";//'失败'

    private GT3GeetestUtilsBind gt3GeetestUtils;

    private GeetestHelper() {
    }

    public void init(Context context, MessageCall messageCall) {
        gt3GeetestUtils = new MyGeetestUtil(context, messageCall);
        // 设置debug模式，开代理抓包可使用，默认关闭，TODO 生产环境务必设置为false
        gt3GeetestUtils.setDebug(BuildConfig.DEBUG);
        // 设置加载webview超时时间，单位毫秒，默认15000，仅且webview加载静态文件超时，不包括之前的http请求
        gt3GeetestUtils.setTimeout(15000);
        // 设置webview请求超时(用户点选或滑动完成，前端请求后端接口)，单位毫秒，默认10000
        gt3GeetestUtils.setWebviewTimeout(10000);
    }

    public GT3GeetestUtilsBind getGt3GeetestUtils() {
        return gt3GeetestUtils;
    }

    public class MyGeetestUtil extends GT3GeetestUtilsBind {


        private MessageCall messageCall;

        public MyGeetestUtil(Context context, MessageCall messageCall) {
            super(context);
            this.messageCall = messageCall;
        }

        @Override
        public void gt3TestFinish() {
            super.gt3TestFinish();
            messageCall.sendMessage(OnCaptchaSuccess);
        }

        public void gt3TestCancel() {
            messageCall.sendMessage(OnCaptchaCancel);
        }

        public void gt3TestError() {
            messageCall.sendMessage(OnCaptchaError);
        }

    }

    public interface MessageCall {
        void sendMessage(String key);
    }
}

