package com.yryz.network;


import android.util.Log;


import java.util.ArrayList;
import java.util.List;

public class LogUtile {

    private static List<String> filtration = new ArrayList<>();

    static {
        filtration.add("NIMLIVE");
    }

  //  private static boolean DEBUG = true && !"prod".equals(BuildConfig.envName);
    private static boolean DEBUG = true ;

    public static void e(String tag, String msg) {
        if (!DEBUG && !filtration.contains(tag)) {
            return;
        }
        Log.e(tag, msg);
    }

    public static void d(String tag, String msg) {
        if (!DEBUG && !filtration.contains(tag)) {
            return;
        }
        Log.d(tag, msg);
    }
}
