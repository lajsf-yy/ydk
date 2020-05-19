package ydk.qrcode.zixing.android;

import android.content.Intent;
import android.os.Handler;

import com.google.zxing.Result;

import ydk.qrcode.zixing.bean.ZxingConfig;
import ydk.qrcode.zixing.camera.CameraManager;
import ydk.qrcode.zixing.view.ViewfinderView;

public interface ICaptureHandler {

    ViewfinderView getViewfinderView();

    CameraManager getCameraManager();

    ZxingConfig getZxingConfig();

    Handler getHandler();

    void handleDecode(Result rawResult);

    void switchFlashImg(int flashState);

    void setScanResult(Intent intent);

    void drawViewfinder();
}
