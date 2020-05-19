package com.yryz.network.http.config;


import android.text.TextUtils;

import com.yryz.network.NetworkConfig;
import com.yryz.network.http.token.DeviceUUID;
import com.yryz.network.http.token.HttpHeader;
import com.yryz.network.http.token.TokenCache;

import java.util.HashMap;
import java.util.Map;

import ydk.core.YdkConfigManager;

public class DefHttpConfiguration implements HttpConfiguration {

    private NetworkConfig networkConfig = YdkConfigManager.getConfig(NetworkConfig.class);


    @Override
    public Map<String, String> getPublicaHeader() {

        Map<String, String> map = new HashMap<>();
        HttpHeader tokenCache = TokenCache.Companion.getHttpHeader();
        map.put("tenantId", networkConfig.getName());
        map.put("devtype", "2");
        map.put("appversion", networkConfig.getAppVersion());
        if (!TextUtils.isEmpty(tokenCache.getUserId())) {
            map.put("userid", tokenCache.getUserId());
        }
        if (!TextUtils.isEmpty(tokenCache.getToken())) {
            map.put("token", tokenCache.getToken());
        }
        map.put("ditchcode", tokenCache.getDitchCode());
        if (!TextUtils.isEmpty(tokenCache.getDevId())) {
            map.put("devid", tokenCache.getDevId());
        } else {
            map.put("devid", DeviceUUID.INSTANCE.loadDeviceId());
        }
        map.put("devName", tokenCache.getDevName());
        map.put("clientVersion", tokenCache.getClientVersion());
        map.put("ip", tokenCache.getIp());
        map.put("User-Agent", "LajsfYyjhApp/1 CFNetwork/975.0.3 Darwin/18.2.0");
        return map;
    }

    @Override
    public long connectTimeout() {
        return 15_000;
    }

    @Override
    public long writeTimeout() {
        return 15_000;
    }

    @Override
    public long readTimeout() {
        return 15_000;
    }


}
