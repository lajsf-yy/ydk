package ydk.captcha;

import android.app.Activity;
import android.text.TextUtils;
import android.util.Log;

import com.geetest.sdk.Bind.GT3GeetestBindListener;
import com.geetest.sdk.Bind.GT3GeetestUtilsBind;
import com.yryz.network.http.HttpClient;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import ydk.annotations.YdkModule;
import ydk.core.Ydk;
import ydk.react.error.ResultException;

@YdkModule(export = true)
public class YdkCaptcha {

    private static String TAG = "YdkCaptcha";

    private GT3GeetestUtilsBind gt3GeetestUtils;

    private CaptchaConfig captchaConfig;


    public YdkCaptcha(CaptchaConfig captchaConfig) {
        this.captchaConfig = captchaConfig;
    }


    private String getApi1() {

        return String.format("%s%s%s%s", captchaConfig.getHttpBaseUrl(), "/platform-support/", captchaConfig.getApiVersion(), "/pb/geetest/action/pre-process");
    }

    private String getApi2() {

        return String.format("%s%s%s%s", captchaConfig.getHttpBaseUrl(), "/platform-support/", captchaConfig.getApiVersion(), "/pb/geetest/action/check");
    }


    /**
     * 开始验证
     */
    public Observable<String> start(String phone, Activity activity) {
        if (null == this.gt3GeetestUtils) {
            GeetestHelper.getInstance().init(Ydk.getApplicationContext(),
                    key -> {
                        Ydk.getEventEmitter().emit(key, new HashMap<String, String>());
                    });
            this.gt3GeetestUtils = GeetestHelper.getInstance().getGt3GeetestUtils();
        }
        return Observable.just(phone)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(s -> {
                    // 开启LoadDialog 第二个参数为lang（语言，如果为null则为系统语言）
                    gt3GeetestUtils.showLoadingDialog(activity, null);
                    // 设置是否可以点击Dialog灰色区域关闭验证码
                    gt3GeetestUtils.setDialogTouch(false);
                    return s;
                })
                .observeOn(Schedulers.io())
                .flatMap((Function<String, ObservableSource<JSONObject>>) s -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("verifyKey", s);
                    return HttpClient.INSTANCE.getClient()
                            .getRetrofitService2()
                            .get(getApi1(), map)
                            .map(responseBody -> {
                                String string = responseBody.string();
                                return new JSONObject(string);
                            });

                })
                .map(jsonObject -> {
                    gt3GeetestUtils.gtSetApi1Json(jsonObject);
                    return jsonObject;
                })
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap((Function<JSONObject, ObservableSource<String>>) jsonObject ->
                        Observable.create(emitter -> gt3GeetestUtils.getGeetest(activity, getApi1(), getApi2(), null, new GT3GeetestBindListener() {
                            /**
                             * @param num 1: 点击验证码的关闭按钮, 2: 点击屏幕关闭验证码, 3: 点击返回键关闭验证码
                             */
                            @Override
                            public void gt3CloseDialog(int num) {
                                Log.i(TAG, "gt3CloseDialog-->num: " + num);
                                if (emitter.isDisposed()) {
                                    return;
                                }
                                emitter.onError(new Exception("cancel"));
                            }

                            /**
                             * 为API1接口添加数据，数据拼接在URL后，API1接口默认get请求
                             */
                            @Override
                            public Map<String, String> gt3CaptchaApi1() {
                                Log.i(TAG, "gt3CaptchaApi1");
                                Map<String, String> map = new HashMap<String, String>();
                                map.put("time", "" + System.currentTimeMillis());
                                return map;
                            }

                            /**
                             * api1接口返回数据
                             */
                            @Override
                            public void gt3FirstResult(JSONObject jsonObject1) {
                                Log.i(TAG, "gt3FirstResult-->" + jsonObject1);
                            }

                            /**
                             * 准备完成，即将弹出验证码
                             */
                            @Override
                            public void gt3DialogReady() {
                                Log.i(TAG, "gt3DialogReady");
                            }

                            /**
                             * 数据统计，从开启验证到成功加载验证码结束，具体解释详见GitHub文档
                             */
                            @Override
                            public void gt3GeetestStatisticsJson(JSONObject jsonObject1) {
                                Log.i(TAG, "gt3GeetestStatisticsJson-->" + jsonObject1);
                            }

                            /**
                             * 返回是否自定义api2，true为自定义api2
                             * false： gt3GetDialogResult(String result)，返回api2需要参数
                             * true： gt3GetDialogResult(boolean a, String result)，返回api2需要的参数
                             */
                            @Override
                            public boolean gt3SetIsCustom() {
                                Log.i(TAG, "gt3SetIsCustom");
                                return true;
                            }

                            /**
                             * 用户滑动或点选完成后触发，gt3SetIsCustom配置为false才走此接口
                             *
                             * @param result api2接口需要参数
                             */
                            @Override
                            public void gt3GetDialogResult(String result) {
                                Log.i(TAG, "gt3GetDialogResult-->" + result);
                            }

                            /**
                             * 用户滑动或点选完成后触发，gt3SetIsCustom配置为true才走此接口
                             *
                             * @param status 验证是否成功
                             * @param result api2接口需要参数
                             */
                            @Override
                            public void gt3GetDialogResult(boolean status, String result) {
                                Log.i(TAG, "gt3GetDialogResult-->status: " + status + "result: " + result);
                                if (status) {
                                    try {
                                        // 1.取出该接口返回的三个参数用于自定义二次验证
                                        JSONObject jsonObject1 = new JSONObject(result);
                                        jsonObject1.put("verifyKey", phone);
                                        // 开启自定义请求api2
                                        emitter.onNext(jsonObject1.toString());
                                        emitter.onComplete();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        gt3GeetestUtils.gt3TestClose();
                                        if (emitter.isDisposed()) {
                                            return;
                                        }
                                        emitter.onError(new Exception("close"));
                                    }
                                } else {
                                    gt3GeetestUtils.gt3TestClose();
                                    if (emitter.isDisposed()) {
                                        return;
                                    }
                                    emitter.onError(new Exception("close"));
                                }
                            }

                            /**
                             * 为API2接口添加数据，数据拼接在URL后，API2接口默认get请求
                             * 默认已有数据：geetest_challenge，geetest_validate，geetest_seccode
                             * TODO 注意： 切勿重复添加以上数据
                             */
                            @Override
                            public Map<String, String> gt3SecondResult() {
                                Log.i(TAG, "gt3SecondResult" + phone);
                                Map<String, String> map = new HashMap<String, String>();
                                return map;
                            }

                            /**
                             * api2完成回调，判断是否验证成功，且成功调用gt3TestFinish，失败调用gt3TestClose
                             *
                             * @param result api2接口返回数据
                             */
                            @Override
                            public void gt3DialogSuccessResult(String result) {
                                Log.i(TAG, "gt3DialogSuccessResult-->" + result);
                                if (!TextUtils.isEmpty(result)) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(result);
                                        String status = jsonObject.getString("status");
                                        if ("success".equals(status)) {
                                            gt3GeetestUtils.gt3TestFinish();
                                            emitter.onNext("success");
                                            emitter.onComplete();
                                        } else {
                                            gt3GeetestUtils.gt3TestClose();
                                            emitter.onError(new Exception("close"));
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        gt3GeetestUtils.gt3TestClose();
                                        if (emitter.isDisposed()) {
                                            return;
                                        }
                                        emitter.onError(new Exception("close"));
                                    }
                                } else {
                                    gt3GeetestUtils.gt3TestClose();
                                    if (emitter.isDisposed()) {
                                        return;
                                    }
                                    emitter.onError(new Exception("close"));
                                }
                            }

                            /**
                             * @param error 返回错误码，具体解释见GitHub文档
                             */
                            @Override
                            public void gt3DialogOnError(String error) {
                                Log.i(TAG, "gt3DialogOnError-->" + error);
                                if (emitter.isDisposed()) {
                                    return;
                                }
                                emitter.onError(new Exception("error" + error));
                            }
                        })))
                .observeOn(Schedulers.io())
                .flatMap((Function<String, ObservableSource<String>>) s -> {
                    Log.e(TAG, "RequestAPI2-->doInBackground: " + s);
                    if (TextUtils.isEmpty(s)) {
                        return Observable.just("");
                    }
                    RequestBody requestBody = RequestBody.create(MediaType.parse("application/json;charset=UTF-8"), s);
                    return HttpClient.INSTANCE.getClient().getRetrofitService2().post(getApi2(), requestBody).map(responseBody -> responseBody.string());

                })
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap((Function<String, ObservableSource<String>>) result ->
                        Observable.create(emitter -> {
                            Log.e(TAG, "RequestAPI2-->onPostExecute: " + result);
                            if (!TextUtils.isEmpty(result)) {
                                try {
                                    JSONObject jsonObject = new JSONObject(result);
                                    String status = jsonObject.getString("status");
                                    if ("success".equals(status)) {
                                        gt3GeetestUtils.gt3TestFinish();
                                        // 设置loading消失回调
                                        gt3GeetestUtils.setGtCallBack(() -> {
                                            // 跳转其他页面操作等
                                            if (emitter.isDisposed()) {
                                                return;
                                            }
                                            emitter.onNext("success");
                                            emitter.onComplete();
                                        });
                                    } else {
                                        gt3GeetestUtils.gt3TestClose();
                                        if (emitter.isDisposed()) {
                                            return;
                                        }
                                        emitter.onError(new Exception("close"));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    gt3GeetestUtils.gt3TestClose();
                                    if (emitter.isDisposed()) {
                                        return;
                                    }
                                    emitter.onError(new Exception("close"));
                                }
                            } else {
                                gt3GeetestUtils.gt3TestClose();
                                if (emitter.isDisposed()) {
                                    return;
                                }
                                emitter.onError(new Exception("close"));
                            }
                        }))
                .retryWhen(throwableObservable ->
                        throwableObservable.flatMap(throwable -> {
                            if (throwable == null) {
                                return Observable.error(throwable);
                            }
                            if (TextUtils.equals("error_12", throwable.getMessage())) {
                                return Observable.error(new ResultException("-20", throwable.getMessage()));
                            }
                            return Observable.error(new ResultException("-1", throwable.getMessage()));
                        }));
    }

    /**
     * 停止
     *
     * @return
     */
    public Observable<Boolean> stop() {
        if (gt3GeetestUtils != null) {
            gt3GeetestUtils.cancelUtils();
        }
        return Observable.just(true);
    }

}
