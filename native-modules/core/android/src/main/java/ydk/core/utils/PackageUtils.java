package ydk.core.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by heus on 2017/11/14.
 */

public class PackageUtils {

    public static final String QQ_NAME = "com.tencent.mobileqq";
    public static final String SINA_NAME = "com.sina.weibo";
    public static final String WECHAT_NAME = "com.tencent.mm";

    public static boolean appInstalled(Context context, String packageName) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return packageInfo != null;
    }

}
