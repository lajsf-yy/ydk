package ydk.core.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.wifi.WifiManager;
import android.os.Build;
import androidx.core.content.ContextCompat;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;


import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import io.reactivex.Observable;
import ydk.core.Ydk;

public class SystemUtils {

    private static final int STATUS_BAR_HEIGHT_M = 24;

    private static final int STATUS_BAR_HEIGHT_L = 25;

    private static final int DEFAULT_TOOLBAR_HEIGHT = 56;

    private static int statusBarHeight = 0;

    private static int topBarHeight = 0;

    /**
     * 获取statebar 高度
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        if (statusBarHeight > 0) {
            return statusBarHeight;
        }
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");

        statusBarHeight = resourceId > 0 ?
                resources.getDimensionPixelSize(resourceId) :
                DensityUtils.dip2px(context, Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? STATUS_BAR_HEIGHT_M : STATUS_BAR_HEIGHT_L);

        return statusBarHeight;
    }

    /**
     * 获取 toop 高度
     *
     * @param context
     * @return
     */
    public static int getTopBarHeight(Context context) {

        if (topBarHeight > 0) {
            return topBarHeight;
        }
        final Resources resources = context.getResources();
        final int resourceId = resources.getIdentifier("action_bar_size", "dimen", "android");
        topBarHeight = resourceId > 0 ?
                resources.getDimensionPixelSize(resourceId) :
                DensityUtils.dip2px(context, DEFAULT_TOOLBAR_HEIGHT);

        return topBarHeight;
    }


    /**
     * 是否有NavigationBar
     *
     * @param context
     * @return
     */
    public static boolean hasNavBar(Context context) {
        boolean hasNavBar = false;
        Resources res = context.getResources();
        int id = res.getIdentifier("config_showNavigationBar", "bool", "android");
        if (id > 0) {
            hasNavBar = res.getBoolean(id);
        }

        try {
            Class<?> sysprop = Class.forName("android.os.SystemProperties");
            Method m = sysprop.getMethod("get", String.class);
            String navBarOverride = (String) m.invoke(sysprop, "qemu.hw.mainkeys");
            if ("1".equals(navBarOverride)) {
                hasNavBar = false;
            } else if ("0".equals(navBarOverride)) {
                hasNavBar = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return hasNavBar;
    }


    /**
     * 获取NavigationBar的高度
     *
     * @param context
     * @return
     */
    public static int getNavBarHeight(Context context) {
        int navBarHeight = 0;
        Resources res = context.getResources();
        int id = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (id > 0 && hasNavBar(context)) {
            navBarHeight = res.getDimensionPixelOffset(id);
        }
        return navBarHeight;
    }

    /**
     * 获取屏幕宽度
     *
     * @param activity
     * @return
     */
    public static final int getScreenWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取屏幕高度
     *
     * @param activity
     * @return
     */
    public static final int getScreenHeight(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 读取清单文件中的配置
     *
     * @return
     */
    public static String getMetaData(Context context, String key) {
        String value = null;
        try {
            ApplicationInfo appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager
                    .GET_META_DATA);
            value = appInfo.metaData.getString(key);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 获取版本名称
     *
     * @return
     */
    public static String getVersionName(Context context) {
        String versionCode = "1.0.0";
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取版本号
     *
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 1;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }


    /**
     * 获得设备id
     *
     * @param activity
     * @return
     */

    public static Observable<String> getDeviceId(final Activity activity) {

        return Ydk.getPermissions(activity, new String[]{Manifest.permission.READ_PHONE_STATE})
                .map(aBoolean -> {
                    String deviceId = "";
                    if (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE)
                            == PackageManager.PERMISSION_GRANTED) {
                        TelephonyManager tm = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
                        deviceId = tm.getDeviceId();
                    }
                    return deviceId;
                });

    }


    /**
     * 获取ip
     *
     * @param activity
     * @return
     */
    public static String getIp(Activity activity) {
        if (activity != null) {
            //获取wifi服务
            WifiManager wifiManager = (WifiManager) activity.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (!wifiManager.isWifiEnabled())
                return "unknow";
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                            return inetAddress.getHostAddress().toString();
                        }
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
        }
        return "unknow";
    }


    private static String intToIp(int i) {
        return (i & 0xFF) + "." +
                ((i >> 8) & 0xFF) + "." +
                ((i >> 16) & 0xFF) + "." +
                (i >> 24 & 0xFF);
    }


    public static boolean inMainProcess(Context context) {
        String packageName = context.getPackageName();
        String processName = getProcessName(context);
        return packageName.equals(processName);
    }

    /**
     * 获取进程名
     *
     * @param appContext
     * @return
     */
    public static String getProcessName(Context appContext) {
        String processName = null;
        int pid = android.os.Process.myPid();
        ActivityManager am = (ActivityManager) appContext.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo p : processes) {
            if (p.pid == pid) {
                processName = p.processName;
            }
        }
        return processName;
    }

}
