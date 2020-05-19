package ydk.qrcode.react;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.Toast;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.google.zxing.Result;

import java.io.File;

import javax.annotation.Nonnull;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import ydk.core.Ydk;
import ydk.core.activityresult.RxActivityResult;
import ydk.core.utils.ImageUtils;
import ydk.core.utils.MapUtils;
import ydk.qrcode.R;
import ydk.qrcode.zixing.android.CaptureActivity;
import ydk.qrcode.zixing.common.Constant;
import ydk.qrcode.zixing.decode.DecodeImgCallback;
import ydk.qrcode.zixing.decode.DecodeImgThread;
import ydk.qrcode.zixing.decode.ImageUtil;
import ydk.qrcode.zixing.encode.CodeCreator;

public class YdkScannerModule extends ReactContextBaseJavaModule {

    public YdkScannerModule(@Nonnull ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Nonnull
    @Override
    public String getName() {
        return "YdkScannerModule";
    }

    @ReactMethod
    public void openScan(Promise promise) {

        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject("500", "扫描失败");
            return;
        }
        Ydk.getPermissions(currentActivity, new String[]{Manifest.permission.CAMERA})
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(aBoolean -> {
                    if (!aBoolean) {
                        return Observable.error(new Exception("没有相机权限"));
                    }
                    return RxActivityResult.on(currentActivity)
                            .startIntent(new Intent(currentActivity, CaptureActivity.class));
                })
                .map(result -> {
                    Intent data = result.data();
                    String content = data.getStringExtra(Constant.CODED_CONTENT);
                    return content;
                })
                .subscribe(s -> {
                    promise.resolve(s);
                }, throwable -> {
                    promise.reject("500", throwable != null ? throwable.getMessage() : "扫描失败");
                });

    }

    @ReactMethod
    public void createQRCode(ReadableMap readableMap, Promise promise) {

        CreateQRCodeBean qrCodeBean = MapUtils.toObject(readableMap.toHashMap(), CreateQRCodeBean.class);

        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject("500", "失败");
            return;
        }
        Ydk.getPermissions(currentActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .observeOn(Schedulers.io())
                .flatMap((Function<Boolean, Observable<Bitmap>>) aBoolean -> {
                    if (!aBoolean) {
                        return Observable.error(new Exception("没有存储权限"));
                    }
                    int height = qrCodeBean.getHeight();
                    int width = qrCodeBean.getWidth();
                    String iconUrl = qrCodeBean.getIconUrl();
                    Bitmap logo = null;
                    if (!TextUtils.isEmpty(iconUrl)) {
                        logo = ImageUtil.getBitmap(new File(iconUrl).getAbsolutePath(), 72, 72);
                    }
                    Bitmap qrCode = CodeCreator.createQRCode(qrCodeBean.getContent(), width == 0 ? 512 : width, height == 0 ? 512 : height, logo);
                    return Observable.just(qrCode);
                }).map(bitmap -> ImageUtils.saveTo(bitmap, "qrCode"))
                .subscribe(s -> {
                    promise.resolve(s);
                }, throwable -> {
                    promise.reject("500", throwable != null ? throwable.getMessage() : "失败");
                });
    }

    @ReactMethod
    public void decodeQRCode(String path, Promise promise) {
        Activity currentActivity = getCurrentActivity();
        if (currentActivity == null) {
            promise.reject("500", "失败");
            return;
        }
        Ydk.getPermissions(currentActivity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE})
                .flatMap((Function<Boolean, Observable<String>>) aBoolean -> {
                    if (!aBoolean) {
                        return Observable.error(new Exception("没有存储权限"));
                    }
                    return Observable.create(emitter ->
                            new DecodeImgThread(path, new DecodeImgCallback() {
                                @Override
                                public void onImageDecodeSuccess(Result result) {
                                    String text = result.getText();
                                    emitter.onNext(text);
                                }

                                @Override
                                public void onImageDecodeFailed() {
                                    emitter.onError(new Exception("解析失败"));
                                }
                            }).run());
                })
                .subscribe(s -> {
                    promise.resolve(s);
                }, throwable -> {
                    promise.reject("500", throwable != null ? throwable.getMessage() : "失败");
                });
    }
}
