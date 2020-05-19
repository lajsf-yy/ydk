package ydk.core.utils;

import android.content.Context;
import android.util.DisplayMetrics;

/**
 * Created by JS01 on 2016/6/7.
 */
public class DensityUtils {

    public static int dip2px(Context context, float var1) {
        float var2 = context.getResources().getDisplayMetrics().density;
        return (int) (var1 * var2 + 0.5F);
    }

    public static int px2dip(Context context, float var1) {
        float var2 = context.getResources().getDisplayMetrics().density;
        return (int) (var1 / var2 + 0.5F);
    }

    public static int sp2px(Context context, float var1) {
        float var2 = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (var1 * var2 + 0.5F);
    }

    public static int px2sp(Context context, float var1) {
        float var2 = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (var1 / var2 + 0.5F);
    }

    public static int getScreenW(Context aty) {
        new DisplayMetrics();
        DisplayMetrics dm = aty.getResources().getDisplayMetrics();
        return dm.widthPixels;
    }

    public static int getScreenH(Context aty) {
        new DisplayMetrics();
        DisplayMetrics dm = aty.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }
}
