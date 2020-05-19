package com.yryz.network.http.network;

import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


public class NeetWork {

    private static Map<String, NetWorkChangListener> mLintenerMap = new HashMap<>();

    public static void registerListener(String key, NetWorkChangListener netWorkChangListener) {
        mLintenerMap.put(key, netWorkChangListener);
    }

    public static void unrRegisterListener(String key) {
        mLintenerMap.remove(key);
    }

    public static void onNetWorkChang(boolean link) {
        if (mLintenerMap == null) {
            return;
        }
        Set<Map.Entry<String, NetWorkChangListener>> entries = mLintenerMap.entrySet();
        Iterator<Map.Entry<String, NetWorkChangListener>> iterator = entries.iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, NetWorkChangListener> next = iterator.next();
            NetWorkChangListener value = next.getValue();
            if (value != null) {
                value.onNetWorkChang(link);
            }
        }
    }

    public static void init(Context context) {
        NetWorkChangReceiver netWorkChangReceiver = new NetWorkChangReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        context.registerReceiver(netWorkChangReceiver, filter);
    }
}
