package ydk.share;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.StringDef;
import android.text.TextUtils;
import android.util.Log;

import com.mob.MobSDK;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import com.tencent.tauth.Tencent;

import java.io.File;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.framework.ShareSDK;
import cn.sharesdk.sina.weibo.SinaWeibo;
import cn.sharesdk.tencent.qq.QQ;
import cn.sharesdk.tencent.qzone.QZone;
import cn.sharesdk.wechat.friends.Wechat;
import cn.sharesdk.wechat.moments.WechatMoments;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import ydk.core.YdkConfigManager;
import ydk.core.utils.FileUtils;
import ydk.core.utils.PackageUtils;
import ydk.share.qq.QQLogin;
import ydk.share.wechat.WeChatLogin;
import ydk.share.wechat.WeChatShare;

/**
 * Created by Gsm on 2018/3/12.
 */

public class YdkShareSDK {

    private static Bitmap mShareBitmap;

    @StringDef({TYPE_QQ, TYPE_QZONE, TYPE_WECHAT, TYPE_WECHAT_MOMENT, TYPE_SINA})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ShareType {
    }

    public static final String TYPE_QQ = "qq";
    public static final String TYPE_QZONE = "qZone";
    public static final String TYPE_WECHAT = "weChat";
    public static final String TYPE_WECHAT_MOMENT = "weChatMoment";
    public static final String TYPE_SINA = "sinaWeibo";

    static HashMap<String, String> sharePlatforms = new HashMap<>();

    static {
        sharePlatforms.put(TYPE_QQ, PackageUtils.QQ_NAME);
        sharePlatforms.put(TYPE_SINA, PackageUtils.SINA_NAME);
        sharePlatforms.put(TYPE_WECHAT, PackageUtils.WECHAT_NAME);
    }

    public static void init(@NonNull Context context) {

        ShareConfig shareConfig = YdkConfigManager.getConfig(ShareConfig.class);

        MobSDK.init(context, shareConfig.getMobAppKey(), shareConfig.getMobAppSecret());
        ShareSDK.setPlatformDevInfo(SinaWeibo.NAME, shareConfig.getSinaConfig());
        ShareSDK.setPlatformDevInfo(Wechat.NAME, shareConfig.getWeChatConfig());
        ShareSDK.setPlatformDevInfo(WechatMoments.NAME, shareConfig.getWeChatConfig());
        ShareSDK.setPlatformDevInfo(QQ.NAME, shareConfig.getQQConfig());
    }

    public static List<String> getSupportPlatforms(@NonNull Context context) {
        List<String> result = new ArrayList<>();
        if (context == null) {
            return result;
        }
        for (Map.Entry<String, String> entry : sharePlatforms.entrySet()) {
            String platform = entry.getKey();
            if (PackageUtils.appInstalled(context, entry.getValue()))
                result.add(platform);
        }
        return result;
    }


    public static Observable<HashMap> share(@NonNull Context context, @ShareType String platformType, @NonNull ShareModel shareModel) {
        return Observable.create((ObservableOnSubscribe) emitter -> share(context, platformType, shareModel, emitter));
    }

    private static void share(Context context, String platformType, ShareModel shareModel, ObservableEmitter emitter) {
        Platform.ShareParams shareParams = new Platform.ShareParams();
        String platformName = "";
        switch (platformType) {
            case TYPE_QQ:
                if (!TextUtils.isEmpty(shareModel.getUrl())) {
                    shareParams.setTitleUrl(shareModel.getUrl());
                }
                platformName = QQ.NAME;
                break;
            case TYPE_QZONE:
                if (!TextUtils.isEmpty(shareModel.getUrl())) {
                    shareParams.setTitleUrl(shareModel.getUrl());
                }
                platformName = QZone.NAME;
                break;
            case TYPE_WECHAT:
                platformName = Wechat.NAME;
                break;
            case TYPE_WECHAT_MOMENT:
                platformName = WechatMoments.NAME;
                break;
            case TYPE_SINA:
                platformName = SinaWeibo.NAME;
                break;
        }
        if (TextUtils.equals(platformName, SinaWeibo.NAME)) {
            shareParams.setText(shareModel.getContent() + (TextUtils.isEmpty(shareModel.getUrl()) ? "" : shareModel.getUrl()));
        } else {
            if (!TextUtils.isEmpty(shareModel.getContent())) {
                shareParams.setText(shareModel.getContent());
            }
            if (!TextUtils.isEmpty(shareModel.getUrl())) {
                shareParams.setUrl(shareModel.getUrl());
            }
        }
        if (!TextUtils.isEmpty(shareModel.getTitle())) {
            shareParams.setTitle(shareModel.getTitle());
            if (TYPE_QQ.equals(platformType) || TYPE_QZONE.equals(platformType)) {
                if (TextUtils.isEmpty(shareModel.getContent())) {
                    shareParams.setText(shareModel.getTitle());
                }
            }
        }
        if (TextUtils.isEmpty(shareModel.getImgUrl())) {
            mShareBitmap = BitmapFactory.decodeResource(context.getResources(), shareModel.getDefImgResId());
            shareParams.setImageData(mShareBitmap);
        } else {
            if (shareModel.getImgUrl().startsWith("file://")) {
                File tempFile = new File(shareModel.getImgUrl().replace("file://", ""));
                File target = new File(Environment.getExternalStoragePublicDirectory("Download").getAbsolutePath(), tempFile.getName());
                FileUtils.copy(tempFile.getAbsolutePath(), target.getAbsolutePath());
                shareParams.setImagePath(target.getAbsolutePath());
            } else {
                shareParams.setImageUrl(shareModel.getImgUrl());
            }
        }

        if ("image".equals(shareModel.getType())) {
            shareParams.setShareType(Platform.SHARE_IMAGE);
        } else if ("audio".equals(shareModel.getType())) {
            shareParams.setShareType(Platform.SHARE_MUSIC);
        } else if ("video".equals(shareModel.getType())) {
            shareParams.setShareType(Platform.SHARE_VIDEO);
        } else {
            if (TextUtils.isEmpty(shareModel.getPath())) {
                if (shareModel.getImgUrl().startsWith("file://")) {
                    shareParams.setShareType(Platform.SHARE_IMAGE);
                } else {
                    shareParams.setShareType(Platform.SHARE_WEBPAGE);
                }
            } else {
                shareParams.setShareType(Platform.SHARE_WXMINIPROGRAM);
                shareParams.setWxPath(shareModel.getPath());
                shareParams.setWxMiniProgramType(shareModel.getMiniProgramType());
            }
        }
        Platform platform = ShareSDK.getPlatform(platformName);
        switch (platformType) {
            case TYPE_WECHAT:
            case TYPE_WECHAT_MOMENT:
                WeChatShare.Companion.setObservableEmitter(emitter);
                break;
            default:
                platform.setPlatformActionListener(new MyPlatformActionListener(emitter));
                break;
        }
        platform.share(shareParams);
    }

    private static void recycleBitmap() {
        if (mShareBitmap != null && !mShareBitmap.isRecycled()) {
            mShareBitmap.recycle();
            mShareBitmap = null;
        }
    }

    /**
     * 移除授权
     *
     * @param platformType
     */
    public static boolean deleteAuthorize(@ShareType String platformType) {
        Platform platform = getAuthPlatform(platformType);
        if (platform.isAuthValid())
            platform.removeAccount(true);
        return true;
    }

    /**
     * 获取授权
     *
     * @param platformType
     */
    public static Observable<HashMap> authorizeLogin(@ShareType String platformType) {
        return Observable.create((ObservableOnSubscribe) emitter -> {
            Platform platform = getAuthPlatform(platformType);
            platform.removeAccount(true);
            platform.setPlatformActionListener(new MyPlatformActionListener(emitter));
            platform.authorize();
        });
    }


    public static Observable authorize(Activity activity, @ShareType String platformType) {
        ShareConfig shareConfig = YdkConfigManager.getConfig(ShareConfig.class);
        switch (platformType) {
            case TYPE_WECHAT:
            case TYPE_WECHAT_MOMENT:
                String wechatAppId = shareConfig.getWechatAppId();
                IWXAPI wxapi = WXAPIFactory.createWXAPI(activity, wechatAppId);
                wxapi.registerApp(wechatAppId);
                return new WeChatLogin().authorize(wxapi);
            case TYPE_QQ:
                Tencent instance = Tencent.createInstance(shareConfig.getQqAppId(), activity.getApplication());
                return new QQLogin().authorize(activity, instance);
            default:
                return Observable.create((ObservableOnSubscribe) emitter -> {
                    emitter.onError(new Exception("暂不支持"));
                });
        }
    }

    private static Platform getAuthPlatform(String platformType) {
        String platformName = "";
        switch (platformType) {
            case TYPE_QQ:
            case TYPE_QZONE:
                platformName = QQ.NAME;
                break;
            case TYPE_WECHAT:
            case TYPE_WECHAT_MOMENT:
                platformName = Wechat.NAME;
                break;
            case TYPE_SINA:
                platformName = SinaWeibo.NAME;
                break;
        }
        return ShareSDK.getPlatform(platformName);
    }

    private static class MyPlatformActionListener implements PlatformActionListener {

        private ObservableEmitter mEmitter;

        public MyPlatformActionListener(ObservableEmitter emitter) {
            mEmitter = emitter;
        }

        @Override
        public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
            Log.e("YdkShareSDK", platform.getName() + " onComplete i >>>>  " + i);

            HashMap<String, Object> map = new HashMap<>();
            if (platform != null && platform.getDb() != null) {
                map.put("userId", platform.getDb().getUserId());
                map.put("userName", platform.getDb().getUserName());
                map.put("token", platform.getDb().getToken());
                map.put("userIcon", platform.getDb().getUserIcon());
                map.put("userGender", platform.getDb().getUserGender());
            }
            mEmitter.onNext(map);
            mEmitter.onComplete();
        }

        @Override
        public void onError(Platform platform, int i, Throwable throwable) {
            String msg = throwable != null ? throwable.getMessage() : "";
            Log.e("YdkShareSDK", platform.getName() + " onError i >>>>  " + i);
            Log.e("YdkShareSDK", platform.getName() + " onError  error with msg " + msg);
            mEmitter.onError(throwable);
        }

        @Override
        public void onCancel(Platform platform, int i) {
            Log.e("YdkShareSDK", platform.getName() + " onCancel i >>>>  " + i);

            mEmitter.onError(new Exception("cancel"));
        }
    }
}
