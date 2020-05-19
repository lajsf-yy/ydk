package ydk.core.utils;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//


import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager.LayoutParams;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class SystemBarTintManager {
    public static final int DEFAULT_TINT_COLOR = -1728053248;
    private static String sNavBarOverride;
    private final SystemBarTintManager.SystemBarConfig mConfig;
    private boolean mStatusBarAvailable;
    private boolean mNavBarAvailable;
    private boolean mStatusBarTintEnabled;
    private boolean mNavBarTintEnabled;
    private View mStatusBarTintView;
    private View mNavBarTintView;

    @TargetApi(19)
    public SystemBarTintManager(Activity var1) {
        Window var2 = var1.getWindow();
        ViewGroup var3 = (ViewGroup) var2.getDecorView();
        if (VERSION.SDK_INT >= 19) {
            int[] var4 = new int[]{16843759, 16843760};
            TypedArray var5 = var1.obtainStyledAttributes(var4);

            try {
                this.mStatusBarAvailable = var5.getBoolean(0, false);
            } finally {
                var5.recycle();
            }

            LayoutParams var6 = var2.getAttributes();
            int var7 = 67108864;
            if ((var6.flags & var7) != 0) {
                this.mStatusBarAvailable = true;
            }

            var7 = 134217728;
            if ((var6.flags & var7) != 0) {
                this.mNavBarAvailable = true;
            }
        }

        this.mConfig = new SystemBarTintManager.SystemBarConfig(var1, this.mStatusBarAvailable, this.mNavBarAvailable);
        if (!this.mConfig.hasNavigtionBar()) {
            this.mNavBarAvailable = false;
        }

        if (this.mStatusBarAvailable) {
            this.setupStatusBarView(var1, var3);
        }

        if (this.mNavBarAvailable) {
            this.setupNavBarView(var1, var3);
        }

    }

    public void setStatusBarTintEnabled(boolean var1) {
        this.mStatusBarTintEnabled = var1;
        if (this.mStatusBarAvailable) {
            this.mStatusBarTintView.setVisibility(var1 ? View.VISIBLE : View.GONE);
        }

    }

    public void setNavigationBarTintEnabled(boolean var1) {
        this.mNavBarTintEnabled = var1;
        if (this.mNavBarAvailable) {
            this.mNavBarTintView.setVisibility(var1 ? View.VISIBLE : View.GONE);
        }

    }

    public void setTintColor(int var1) {
        this.setStatusBarTintColor(var1);
        this.setNavigationBarTintColor(var1);
    }

    public void setTintResource(int var1) {
        this.setStatusBarTintResource(var1);
        this.setNavigationBarTintResource(var1);
    }

    public void setTintDrawable(Drawable var1) {
        this.setStatusBarTintDrawable(var1);
        this.setNavigationBarTintDrawable(var1);
    }

    public void setTintAlpha(float var1) {
        this.setStatusBarAlpha(var1);
        this.setNavigationBarAlpha(var1);
    }

    public void setStatusBarTintColor(int var1) {
        if (this.mStatusBarAvailable) {
            this.mStatusBarTintView.setBackgroundColor(var1);
        }

    }

    public void setStatusBarTintResource(int var1) {
        if (this.mStatusBarAvailable) {
            this.mStatusBarTintView.setBackgroundResource(var1);
        }

    }

    public void setStatusBarTintDrawable(Drawable var1) {
        if (this.mStatusBarAvailable) {
            this.mStatusBarTintView.setBackgroundDrawable(var1);
        }

    }

    @TargetApi(11)
    public void setStatusBarAlpha(float var1) {
        if (this.mStatusBarAvailable && VERSION.SDK_INT >= 11) {
            this.mStatusBarTintView.setAlpha(var1);
        }

    }

    public void setNavigationBarTintColor(int var1) {
        if (this.mNavBarAvailable) {
            this.mNavBarTintView.setBackgroundColor(var1);
        }

    }

    public void setNavigationBarTintResource(int var1) {
        if (this.mNavBarAvailable) {
            this.mNavBarTintView.setBackgroundResource(var1);
        }

    }

    public void setNavigationBarTintDrawable(Drawable var1) {
        if (this.mNavBarAvailable) {
            this.mNavBarTintView.setBackgroundDrawable(var1);
        }

    }

    @TargetApi(11)
    public void setNavigationBarAlpha(float var1) {
        if (this.mNavBarAvailable && VERSION.SDK_INT >= 11) {
            this.mNavBarTintView.setAlpha(var1);
        }

    }

    public SystemBarTintManager.SystemBarConfig getConfig() {
        return this.mConfig;
    }

    public boolean isStatusBarTintEnabled() {
        return this.mStatusBarTintEnabled;
    }

    public boolean isNavBarTintEnabled() {
        return this.mNavBarTintEnabled;
    }

    private void setupStatusBarView(Context var1, ViewGroup var2) {
        this.mStatusBarTintView = new View(var1);
        android.widget.FrameLayout.LayoutParams var3 = new android.widget.FrameLayout.LayoutParams(-1, this.mConfig.getStatusBarHeight());
        var3.gravity = 48;
        if (this.mNavBarAvailable && !this.mConfig.isNavigationAtBottom()) {
            var3.rightMargin = this.mConfig.getNavigationBarWidth();
        }

        this.mStatusBarTintView.setLayoutParams(var3);
        this.mStatusBarTintView.setBackgroundColor(-1728053248);
        this.mStatusBarTintView.setVisibility(View.GONE);
        var2.addView(this.mStatusBarTintView);
    }

    private void setupNavBarView(Context var1, ViewGroup var2) {
        this.mNavBarTintView = new View(var1);
        android.widget.FrameLayout.LayoutParams var3;
        if (this.mConfig.isNavigationAtBottom()) {
            var3 = new android.widget.FrameLayout.LayoutParams(-1, this.mConfig.getNavigationBarHeight());
            var3.gravity = 80;
        } else {
            var3 = new android.widget.FrameLayout.LayoutParams(this.mConfig.getNavigationBarWidth(), -1);
            var3.gravity = 5;
        }

        this.mNavBarTintView.setLayoutParams(var3);
        this.mNavBarTintView.setBackgroundColor(-1728053248);
        this.mNavBarTintView.setVisibility(View.GONE);
        var2.addView(this.mNavBarTintView);
    }

    public static int StatusBarLightMode(Activity var0) {
        byte var1 = 0;
        if (VERSION.SDK_INT >= 19) {
            if (MIUISetStatusBarLightMode(var0.getWindow(), true)) {
                var1 = 1;
            } else if (FlymeSetStatusBarLightMode(var0.getWindow(), true)) {
                var1 = 2;
            } else if (VERSION.SDK_INT >= 23) {
                var0.getWindow().getDecorView().setSystemUiVisibility(9216);
                var1 = 3;
            }
        }

        return var1;
    }

    public static void StatusBarLightMode(Activity var0, int var1) {
        if (var1 == 1) {
            MIUISetStatusBarLightMode(var0.getWindow(), true);
        } else if (var1 == 2) {
            FlymeSetStatusBarLightMode(var0.getWindow(), true);
        } else if (var1 == 3 && VERSION.SDK_INT >= 23) {
            var0.getWindow().getDecorView().setSystemUiVisibility(9216);
        }

    }

    public static void StatusBarDarkMode(Activity var0, int var1) {
        if (var1 == 1) {
            MIUISetStatusBarLightMode(var0.getWindow(), false);
        } else if (var1 == 2) {
            FlymeSetStatusBarLightMode(var0.getWindow(), false);
        } else if (var1 == 3 && VERSION.SDK_INT >= 14) {
            var0.getWindow().getDecorView().setSystemUiVisibility(0);
        }

    }

    public static boolean FlymeSetStatusBarLightMode(Window var0, boolean var1) {
        boolean var2 = false;
        if (var0 != null) {
            try {
                LayoutParams var3 = var0.getAttributes();
                Field var4 = LayoutParams.class.getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field var5 = LayoutParams.class.getDeclaredField("meizuFlags");
                var4.setAccessible(true);
                var5.setAccessible(true);
                int var6 = var4.getInt((Object) null);
                int var7 = var5.getInt(var3);
                if (var1) {
                    var7 |= var6;
                } else {
                    var7 &= ~var6;
                }

                var5.setInt(var3, var7);
                var0.setAttributes(var3);
                var2 = true;
            } catch (Exception var8) {
                ;
            }
        }

        return var2;
    }

    public static boolean MIUISetStatusBarLightMode(Window var0, boolean var1) {
        boolean var2 = false;
        if (var0 != null) {
            Class var3 = var0.getClass();

            try {
                boolean var4 = false;
                Class var5 = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field var6 = var5.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                int var9 = var6.getInt(var5);
                Method var7 = var3.getMethod("setExtraFlags", Integer.TYPE, Integer.TYPE);
                if (var1) {
                    var7.invoke(var0, var9, var9);
                } else {
                    var7.invoke(var0, 0, var9);
                }

                var2 = true;
            } catch (Exception var8) {
                ;
            }
        }

        return var2;
    }

    static {
        if (VERSION.SDK_INT >= 19) {
            try {
                Class var0 = Class.forName("android.os.SystemProperties");
                Method var1 = var0.getDeclaredMethod("get", String.class);
                var1.setAccessible(true);
                sNavBarOverride = (String) var1.invoke((Object) null, "qemu.hw.mainkeys");
            } catch (Throwable var2) {
                sNavBarOverride = null;
            }
        }

    }

    public static class SystemBarConfig {
        private static final String STATUS_BAR_HEIGHT_RES_NAME = "status_bar_height";
        private static final String NAV_BAR_HEIGHT_RES_NAME = "navigation_bar_height";
        private static final String NAV_BAR_HEIGHT_LANDSCAPE_RES_NAME = "navigation_bar_height_landscape";
        private static final String NAV_BAR_WIDTH_RES_NAME = "navigation_bar_width";
        private static final String SHOW_NAV_BAR_RES_NAME = "config_showNavigationBar";
        private final boolean mTranslucentStatusBar;
        private final boolean mTranslucentNavBar;
        private final int mStatusBarHeight;
        private final int mActionBarHeight;
        private final boolean mHasNavigationBar;
        private final int mNavigationBarHeight;
        private final int mNavigationBarWidth;
        private final boolean mInPortrait;
        private final float mSmallestWidthDp;

        private SystemBarConfig(Activity var1, boolean var2, boolean var3) {
            Resources var4 = var1.getResources();
            this.mInPortrait = var4.getConfiguration().orientation == 1;
            this.mSmallestWidthDp = this.getSmallestWidthDp(var1);
            this.mStatusBarHeight = this.getInternalDimensionSize(var4, "status_bar_height");
            this.mActionBarHeight = this.getActionBarHeight(var1);
            this.mNavigationBarHeight = this.getNavigationBarHeight(var1);
            this.mNavigationBarWidth = this.getNavigationBarWidth(var1);
            this.mHasNavigationBar = this.mNavigationBarHeight > 0;
            this.mTranslucentStatusBar = var2;
            this.mTranslucentNavBar = var3;
        }

        @TargetApi(14)
        private int getActionBarHeight(Context var1) {
            int var2 = 0;
            if (VERSION.SDK_INT >= 14) {
                TypedValue var3 = new TypedValue();
                var1.getTheme().resolveAttribute(16843499, var3, true);
                var2 = TypedValue.complexToDimensionPixelSize(var3.data, var1.getResources().getDisplayMetrics());
            }

            return var2;
        }

        @TargetApi(14)
        private int getNavigationBarHeight(Context var1) {
            Resources var2 = var1.getResources();
            byte var3 = 0;
            if (VERSION.SDK_INT >= 14 && this.hasNavBar(var1)) {
                String var4;
                if (this.mInPortrait) {
                    var4 = "navigation_bar_height";
                } else {
                    var4 = "navigation_bar_height_landscape";
                }

                return this.getInternalDimensionSize(var2, var4);
            } else {
                return var3;
            }
        }

        @TargetApi(14)
        private int getNavigationBarWidth(Context var1) {
            Resources var2 = var1.getResources();
            byte var3 = 0;
            return VERSION.SDK_INT >= 14 && this.hasNavBar(var1) ? this.getInternalDimensionSize(var2, "navigation_bar_width") : var3;
        }

        @TargetApi(14)
        private boolean hasNavBar(Context var1) {
            Resources var2 = var1.getResources();
            int var3 = var2.getIdentifier("config_showNavigationBar", "bool", "android");
            if (var3 != 0) {
                boolean var4 = var2.getBoolean(var3);
                if ("1".equals(SystemBarTintManager.sNavBarOverride)) {
                    var4 = false;
                } else if ("0".equals(SystemBarTintManager.sNavBarOverride)) {
                    var4 = true;
                }

                return var4;
            } else {
                return !ViewConfiguration.get(var1).hasPermanentMenuKey();
            }
        }

        private int getInternalDimensionSize(Resources var1, String var2) {
            int var3 = 0;
            int var4 = var1.getIdentifier(var2, "dimen", "android");
            if (var4 > 0) {
                var3 = var1.getDimensionPixelSize(var4);
            }

            return var3;
        }

        @SuppressLint({"NewApi"})
        private float getSmallestWidthDp(Activity var1) {
            DisplayMetrics var2 = new DisplayMetrics();
            if (VERSION.SDK_INT >= 16) {
                var1.getWindowManager().getDefaultDisplay().getRealMetrics(var2);
            } else {
                var1.getWindowManager().getDefaultDisplay().getMetrics(var2);
            }

            float var3 = (float) var2.widthPixels / var2.density;
            float var4 = (float) var2.heightPixels / var2.density;
            return Math.min(var3, var4);
        }

        public boolean isNavigationAtBottom() {
            return this.mSmallestWidthDp >= 600.0F || this.mInPortrait;
        }

        public int getStatusBarHeight() {
            return this.mStatusBarHeight;
        }

        public int getActionBarHeight() {
            return this.mActionBarHeight;
        }

        public boolean hasNavigtionBar() {
            return this.mHasNavigationBar;
        }

        public int getNavigationBarHeight() {
            return this.mNavigationBarHeight;
        }

        public int getNavigationBarWidth() {
            return this.mNavigationBarWidth;
        }

        public int getPixelInsetTop(boolean var1) {
            return (this.mTranslucentStatusBar ? this.mStatusBarHeight : 0) + (var1 ? this.mActionBarHeight : 0);
        }

        public int getPixelInsetBottom() {
            return this.mTranslucentNavBar && this.isNavigationAtBottom() ? this.mNavigationBarHeight : 0;
        }

        public int getPixelInsetRight() {
            return this.mTranslucentNavBar && !this.isNavigationAtBottom() ? this.mNavigationBarWidth : 0;
        }
    }
}
