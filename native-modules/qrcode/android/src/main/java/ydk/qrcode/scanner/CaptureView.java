package ydk.qrcode.scanner;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;

import com.google.zxing.Result;

import java.io.IOException;

import ydk.core.Ydk;
import ydk.qrcode.R;
import ydk.qrcode.zixing.android.BeepManager;
import ydk.qrcode.zixing.android.CaptureHandler;
import ydk.qrcode.zixing.android.FinishListener;
import ydk.qrcode.zixing.android.ICaptureHandler;
import ydk.qrcode.zixing.android.InactivityTimer;
import ydk.qrcode.zixing.bean.ZxingConfig;
import ydk.qrcode.zixing.camera.CameraManager;
import ydk.qrcode.zixing.common.Constant;
import ydk.qrcode.zixing.view.ViewfinderView;


public class CaptureView extends FrameLayout implements View.OnClickListener, SurfaceHolder.Callback, ICaptureHandler, ViewTreeObserver.OnGlobalLayoutListener {

    private static final String TAG = CaptureView.class.getSimpleName();

    public ZxingConfig config = new ZxingConfig();
    private SurfaceView previewView;
    private ViewfinderView viewfinderView;
    private AppCompatImageView flashLightIv;
    private TextView flashLightTv;
    private AppCompatImageView backIv;
    private LinearLayoutCompat flashLightLayout;
    private LinearLayoutCompat albumLayout;
    private LinearLayoutCompat bottomLayout;
    private boolean hasSurface;
    private InactivityTimer inactivityTimer;
    private BeepManager beepManager;
    private CameraManager cameraManager;
    private CaptureHandler handler;
    private SurfaceHolder surfaceHolder;
    private Activity mActivity;
    private ScannerResultListener scannerResultListener;
    private ScannerLifecycleCallbacks lifecycleCallbacks;


    @Override
    public ViewfinderView getViewfinderView() {
        return viewfinderView;
    }

    @Override
    public Handler getHandler() {
        return handler;
    }

    @Override
    public CameraManager getCameraManager() {
        return cameraManager;
    }

    @Override
    public ZxingConfig getZxingConfig() {
        return config;
    }

    @Override
    public void drawViewfinder() {
        viewfinderView.drawViewfinder();
    }

    public void setScannerResultListener(ScannerResultListener resultListener) {
        this.scannerResultListener = resultListener;
    }


    public CaptureView(Activity activity) {
        super(activity);
        this.mActivity = activity;
        onCreate();
    }


    private void onCreate() {

        lifecycleCallbacks = new ScannerLifecycleCallbacks();
        Ydk.getApplicationContext().registerActivityLifecycleCallbacks(lifecycleCallbacks);

        initView();

        hasSurface = false;

        inactivityTimer = new InactivityTimer(mActivity);
        beepManager = new BeepManager(mActivity);
        beepManager.setPlayBeep(config.isPlayBeep());
        beepManager.setVibrate(config.isShake());


    }

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.activity_capture, this);


        previewView = findViewById(R.id.preview_view);
        previewView.setOnClickListener(this);

        viewfinderView = findViewById(R.id.viewfinder_view);
        viewfinderView.setZxingConfig(config);


        backIv = findViewById(R.id.backIv);
        backIv.setVisibility(GONE);
        backIv.setOnClickListener(this);

        flashLightIv = findViewById(R.id.flashLightIv);
        flashLightTv = findViewById(R.id.flashLightTv);
        flashLightLayout = findViewById(R.id.flashLightLayout);
        flashLightLayout.setVisibility(GONE);
        flashLightLayout.setOnClickListener(this);
        albumLayout = findViewById(R.id.albumLayout);
        albumLayout.setVisibility(GONE);
        albumLayout.setOnClickListener(this);

        findViewById(R.id.headerLayout).setVisibility(GONE);
        bottomLayout = findViewById(R.id.bottomLayout);
        bottomLayout.setVisibility(GONE);

        // switchVisibility(bottomLayout, config.isShowbottomLayout());
        //   switchVisibility(flashLightLayout, config.isShowFlashLight());
        //  switchVisibility(albumLayout, config.isShowAlbum());
          getViewTreeObserver().addOnGlobalLayoutListener(this);

        /*有闪光灯就显示手电筒按钮  否则不显示*/
//        if (isSupportCameraLedFlash(getContext().getPackageManager())) {
//            flashLightLayout.setVisibility(View.VISIBLE);
//        } else {
//            flashLightLayout.setVisibility(View.GONE);
//        }

    }


    public void turnOnFlashLight() {
        cameraManager.switchFlashLight(handler);
    }

    public void setScannerConfig(ScannerConfig config) {

        ZxingConfig zxingConfig = new ZxingConfig();
        zxingConfig.setReactColor(Color.parseColor(config.angleColor));
        zxingConfig.setScanLineColor(Color.parseColor(config.lineColor));
        zxingConfig.setFrameLineColor(Color.parseColor(config.boxColor));
        viewfinderView.setZxingConfig(zxingConfig);
    }

    /**
     * @param pm
     * @return 是否有闪光灯
     */
    public static boolean isSupportCameraLedFlash(PackageManager pm) {
        if (pm != null) {
            FeatureInfo[] features = pm.getSystemAvailableFeatures();
            if (features != null) {
                for (FeatureInfo f : features) {
                    if (f != null && PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * @param flashState 切换闪光灯图片
     */
    @Override
    public void switchFlashImg(int flashState) {

        if (flashState == Constant.FLASH_OPEN) {
            flashLightIv.setImageResource(R.drawable.ic_open);
            flashLightTv.setText(R.string.close_flash);
        } else {
            flashLightIv.setImageResource(R.drawable.ic_close);
            flashLightTv.setText(R.string.open_flash);
        }

    }

    @Override
    public void setScanResult(Intent intent) {
        String content = intent.getStringExtra(Constant.CODED_CONTENT);
        scannerResultListener.onScannerResult(content);
    }

    /**
     * @param rawResult 返回的扫描结果
     */
    @Override
    public void handleDecode(Result rawResult) {

        inactivityTimer.onActivity();

        beepManager.playBeepSoundAndVibrate();

        scannerResultListener.onScannerResult(rawResult.getText());

    }


    private void switchVisibility(View view, boolean b) {
        if (b) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }

    @Override
    public void onGlobalLayout() {
        onResume();
    }


    public void onResume() {

        if (cameraManager == null) {
            cameraManager = new CameraManager(getContext().getApplicationContext(), config);
        }

        viewfinderView.setCameraManager(cameraManager);
        handler = null;

        surfaceHolder = previewView.getHolder();
        if (hasSurface) {

            initCamera(surfaceHolder);
        } else {
            // 重置callback，等待surfaceCreated()来初始化camera
            surfaceHolder.addCallback(this);
        }

        beepManager.updatePrefs();
        inactivityTimer.onResume();

    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        if (surfaceHolder == null) {
            throw new IllegalStateException("No SurfaceHolder provided");
        }
        if (cameraManager.isOpen()) {
            return;
        }
        try {
            // 打开Camera硬件设备
            cameraManager.openDriver(surfaceHolder);
            // 创建一个handler来打开预览，并抛出一个运行时异常
            if (handler == null) {
                handler = new CaptureHandler(this, cameraManager);
            }
        } catch (IOException ioe) {
            Log.w(TAG, ioe);
            displayFrameworkBugMessageAndExit();
        } catch (RuntimeException e) {
            Log.w(TAG, "Unexpected error initializing camera", e);
            displayFrameworkBugMessageAndExit();
        }
    }

    private void displayFrameworkBugMessageAndExit() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("扫一扫");
        builder.setMessage(getContext().getString(R.string.msg_camera_framework_bug));
        builder.setPositiveButton(R.string.button_ok, new FinishListener(mActivity));
        builder.setOnCancelListener(new FinishListener(mActivity));
        builder.show();
    }


    public void onPause() {

        Log.i("CaptureActivity", "onPause");
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        inactivityTimer.onPause();
        beepManager.close();
        cameraManager.closeDriver();

        if (!hasSurface) {

            surfaceHolder.removeCallback(this);
        }

    }

    public void onDestroy() {

        onPause();
        inactivityTimer.shutdown();
        viewfinderView.stopAnimator();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        Ydk.getApplicationContext().unregisterActivityLifecycleCallbacks(lifecycleCallbacks);

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(holder);
        }
    }


    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {

    }

    @Override
    public void onClick(View view) {

        int id = view.getId();
        if (id == R.id.flashLightLayout) {
            /*切换闪光灯*/
            cameraManager.switchFlashLight(handler);
        } else if (id == R.id.albumLayout) {
            /*打开相册*/
            // Intent intent = new Intent();
            //  intent.setAction(Intent.ACTION_PICK);
            // intent.setType("image/*");
            // activity.startActivityForResult(intent, Constant.REQUEST_IMAGE);
        } else if (id == R.id.backIv) {
            //  activity.finish();
        }
    }


    class ScannerLifecycleCallbacks implements Application.ActivityLifecycleCallbacks {

        @Override
        public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        }

        @Override
        public void onActivityStarted(Activity activity) {

        }

        @Override
        public void onActivityResumed(Activity activity) {
            if (mActivity == activity) {
                onResume();
            }
        }

        @Override
        public void onActivityPaused(Activity activity) {
            if (mActivity == activity) {
                onPause();
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        }

        @Override
        public void onActivityDestroyed(Activity activity) {

            if (mActivity == activity) {
                onDestroy();
            }
        }
    }

}
