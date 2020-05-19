package ydk.core.utils;


import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.os.Build;
import androidx.annotation.RequiresApi;
import android.util.Log;
import android.view.DisplayCutout;



import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class LiuhaiUtils {
//
//            <!--允许绘制到oppo、vivo刘海屏机型的刘海区域 -->
//        <meta-data
//    android:name="android.max_aspect"
//    android:value="2.2" />
//
//        <!-- 允许绘制到华为刘海屏机型的刘海区域 -->
//        <meta-data
//    android:name="android.notch_support"
//    android:value="true" />
//
//        <!-- 允许绘制到小米刘海屏机型的刘海区域 -->
//        <meta-data
//    android:name="notch.config"
//    android:value="portrait" />


    private static final String TAG = "LiuhaiUtils";


    private static Boolean hasNotch;

    private static int[] sizeArray;

    /**
     * 其他手机型号TODU
     *
     * @param context 当前 activity
     * @return
     */
    public static boolean hasNotch(Context context) {
        if (hasNotch == null) {
            // 26 以下
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

                return hasNotch = false;
            }
            //28 以及以上   9.0系统全屏界面默认会保留黑边，不允许显示内容到刘海区域
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                hasNotch = hasNotchP((Activity) context);
                return hasNotch;
            }
            //26 and 27
            if (Rom.isEmui()) {
                hasNotch = hasNotchInHuawei(context);
            } else if (Rom.isMiui()) {
                hasNotch = hasNotchInMiui();
            } else if (Rom.isOppo()) {
                hasNotch = hasNotchInOppo(context);
            } else if (Rom.isVivo()) {
                hasNotch = hasNotchInVivo(context);
            } else {
                hasNotch = false;
            }
        }
        return hasNotch;
    }

    /**
     * 获取刘海
     *
     * @return
     */
    public static int[] getNotchSize(Context context) {

        if (sizeArray == null) {
            sizeArray = new int[]{0, 0};
            // 26 以下
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {

                return sizeArray;
            }
            //28 以及以上   9.0系统全屏界面默认会保留黑边，不允许显示内容到刘海区域
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                return sizeArray = getNotchSizeO((Activity) context);
            }
            // 27 and 28
            if (Rom.isEmui()) {
                return sizeArray = getNotchSizeInHuawei(context);
            } else if (Rom.isMiui()) {
                return sizeArray = getNotchSizeInMiui(context);
            } else if (Rom.isOppo()) {
                return sizeArray = getNotchSizeInOppo(context);
            } else if (Rom.isVivo()) {
                return sizeArray = getNotchSizeInVivo(context);
            }
        }
        return sizeArray;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private static boolean hasNotchP(Activity activity) {

        DisplayCutout displayCutout = activity.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout();

        if (displayCutout == null) {
            return false;
        }
        List<Rect> rects = displayCutout.getBoundingRects();

        return rects != null && rects.size() > 0;

    }


    /**
     * 判断该华为手机是否刘海屏
     *
     * @param context
     * @return
     */
    private static boolean hasNotchInHuawei(Context context) {
        boolean hasNotch = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method hasNotchInScreen = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            if (hasNotchInScreen != null) {
                hasNotch = (boolean) hasNotchInScreen.invoke(HwNotchSizeUtil);
            }
        } catch (Exception e) {
            Log.e(TAG, " hasNotchInHuawei  " + e.getMessage());
        }
        return hasNotch;
    }

    /**
     * @param context
     * @return
     */
    private static int[] getNotchSizeInHuawei(Context context) {
        int[] ret = new int[]{0, 0};
        try {
            ClassLoader cl = context.getClassLoader();
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method get = HwNotchSizeUtil.getMethod("getNotchSize");
            ret = (int[]) get.invoke(HwNotchSizeUtil);
            if (ret == null) {
                ret = new int[]{0, 0};
            }
            ret[0] = px2dip(context, ret[0]);
            ret[1] = px2dip(context, ret[1]);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getNotchSize ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "getNotchSize NoSuchMethodException");
        } catch (Exception e) {
            Log.e(TAG, "getNotchSize Exception");
        }
        return ret;
    }

    /**
     * 判断该 OPPO 手机是否为刘海屏手机
     *
     * @param context
     * @return
     */
    private static boolean hasNotchInOppo(Context context) {
        return context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
    }

    /**
     * 获取OPPO刘海屏的高度
     * 对于 OPPO 刘海屏手机的刘海高度，OPPO 官方的文档没有提供相关的 API，
     * 但官方文档表示 OPPO 手机的刘海高度和状态栏的高度是一致的，
     * 所以我们可以直接获取状态栏的高度，作为 OPPO 手机的刘海高度
     * 采用宽度为1080px,  高度为2280px的圆弧显示屏。 屏幕顶部凹形区域不能显示内容，宽度为324px,  高度为80px
     *
     * @param context
     * @return
     */
    private static int[] getNotchSizeInOppo(Context context) {
        int[] size = new int[]{0, 0};
        if (!hasNotchInOppo(context)) {
            return size;
        }
        size[0] = px2dip(context, 324);
        size[1] = px2dip(context, getStatusBarHeight(context));
        return size;
    }

    /**
     * 判断该 vivo 手机是否为刘海屏手机
     *
     * @param context
     * @return
     */
    private static boolean hasNotchInVivo(Context context) {
        boolean hasNotch = false;
        try {
            ClassLoader cl = context.getClassLoader();
            Class ftFeature = cl.loadClass("android.util.FtFeature");
            Method[] methods = ftFeature.getDeclaredMethods();
            if (methods != null) {
                for (int i = 0; i < methods.length; i++) {
                    Method method = methods[i];
                    if (method != null) {
                        if (method.getName().equalsIgnoreCase("isFeatureSupport")) {
                            hasNotch = (boolean) method.invoke(ftFeature, 0x00000020);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            hasNotch = false;
            Log.e(TAG, "hasNotchInVivo " + e.getMessage());
        }
        return hasNotch;
    }

    /**
     * 官方文档为100dp,27 dp
     *
     * @param context
     * @return
     */
    private static int[] getNotchSizeInVivo(Context context) {
        int[] ret = new int[]{0, 0};
        if (!hasNotchInVivo(context)) {
            return ret;
        }
        ret[0] = 100;
        ret[1] = 27;
        return ret;
    }

    /**
     * 判断小米Ui的刘海屏
     *
     * @return
     */
    private static boolean hasNotchInMiui() {

        Class<?> classT = null;
        try {
            classT = Class.forName("android.os.SystemProperties");
            Method m = classT.getMethod("getInt", new Class[]{String.class, int.class});
            int hsaTouch = (Integer) m.invoke(classT, new Object[]{"ro.miui.notch", 0});
            return hsaTouch == 1;
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "hasNotchInMiui " + e.getMessage());
        } catch (NoSuchMethodException e) {
            Log.e(TAG, "hasNotchInMiui " + e.getMessage());
        } catch (IllegalAccessException e) {
            Log.e(TAG, "hasNotchInMiui " + e.getMessage());
        } catch (InvocationTargetException e) {
            Log.e(TAG, "hasNotchInMiui " + e.getMessage());
        }
        return false;
    }

    /**
     * 获取小米手机的刘海宽高
     *
     * @param context
     * @return
     */
    private static int[] getNotchSizeInMiui(Context context) {
        int[] size = new int[]{0, 0};
        int notch_width = context.getResources().getIdentifier("notch_width", "dimen", "android");
        if (notch_width > 0) {
            size[0] = px2dip(context, context.getResources().getDimensionPixelSize(notch_width));
        }
        int notch_height = context.getResources().getIdentifier("notch_height", "dimen", "android");
        if (notch_height > 0) {
            size[1] = px2dip(context, context.getResources().getDimensionPixelSize(notch_height));
        }
        return size;
    }


    /**
     * 获取状态栏高度
     *
     * @param context
     * @return
     */
    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    @RequiresApi(api = Build.VERSION_CODES.P)
    private static int[] getNotchSizeO(Activity activity) {
        int[] size = new int[]{0, 0};
        DisplayCutout displayCutout = activity.getWindow().getDecorView().getRootWindowInsets().getDisplayCutout();
        List<Rect> rects = displayCutout.getBoundingRects();
        if (rects == null || rects.isEmpty()) {
            return size;
        }
        Rect rect = rects.get(0);
        size[0] = px2dip(activity, rect.right - rect.left);
        size[1] = px2dip(activity, rect.bottom - rect.top);
        return size;
    }


    /**
     * dp转换为px
     *
     * @param context
     * @param dpValue
     * @return
     */
    private static int dpToPx(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    /**
     * px 转 dp
     * 48px - 16dp
     * 50px - 17dp
     */
    private static int px2dip(Context context, float pxValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) ((pxValue / scale) + 0.5f);
    }


}