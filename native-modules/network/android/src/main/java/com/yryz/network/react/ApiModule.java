package com.yryz.network.react;

import android.text.TextUtils;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.yryz.network.http.model.AuthTokenVO;
import com.yryz.network.http.model.RefreshTokenVo;
import com.yryz.network.http.token.HttpHeader;
import com.yryz.network.http.token.RefreshToken;
import com.yryz.network.http.token.TokenCache;
import com.yryz.network.http.token.TokenController;
import com.yryz.network.http.token.TokenEnum;

import ydk.core.utils.MapUtils;


public class ApiModule extends ReactContextBaseJavaModule {

    /**
     * 其他错误
     */
    private static final String AUTHERROR = "authError";

    private static ReactApplicationContext reactApplicationContext;

    public ApiModule(ReactApplicationContext reactContext) {
        super(reactContext);
        reactApplicationContext = reactContext;
    }

    @Override
    public String getName() {
        return "ApiModule";
    }

    @ReactMethod
    public void httpHeader(ReadableMap readableMap) {

        if (readableMap == null) {
            return;
        }

        HttpHeader httpHeader = MapUtils.toObject(readableMap.toHashMap(), HttpHeader.class);


        TokenCache.Companion.refreshToken(httpHeader);

    }


    @ReactMethod
    public void refreshToken(ReadableMap readableMap, Promise promise) {

        if (readableMap == null) {
            return;
        }
        AuthTokenVO authTokenVO = MapUtils.toObject(readableMap.toHashMap(), AuthTokenVO.class);

        HttpHeader httpHeader = TokenCache.Companion.getHttpHeader();

        if (authTokenVO == null || authTokenVO.getToken() == null) {
            RefreshToken refreshToken = new RefreshToken(200, httpHeader.getRefreshToken(), httpHeader.getToken());
            promise.resolve(refreshTokenToWritableMap(refreshToken));
            return;
        }
        //token与本地的相等，直接刷
        if (authTokenVO.getToken().equals(httpHeader.getToken())) {
            TokenController.Companion.rnRenewToken((integer, newHttpHeader) -> {
                if (integer.intValue() == TokenEnum.CODE_200.getCode()) {
                    RefreshToken refreshToken = new RefreshToken(200, newHttpHeader.getRefreshToken(), newHttpHeader.getToken());
                    promise.resolve(refreshTokenToWritableMap(refreshToken));
                } else {
                    promise.reject("500", "token刷新失败");
                }
                return null;
            });

        } else {

            RefreshToken refreshToken = new RefreshToken(200, httpHeader.getRefreshToken(), httpHeader.getToken());

            promise.resolve(refreshTokenToWritableMap(refreshToken));
        }


    }


    private static WritableMap refreshTokenToWritableMap(RefreshToken refreshToken) {

        WritableMap writableMap = Arguments.createMap();
        if (refreshToken.getCode() != 0) {
            writableMap.putInt("code", refreshToken.getCode());
        }
        if (!TextUtils.isEmpty(refreshToken.getRefreshToken())) {
            writableMap.putString("refreshToken", refreshToken.getRefreshToken());
        }
        if (!TextUtils.isEmpty(refreshToken.getToken())) {
            writableMap.putString("token", refreshToken.getToken());
        }
        return writableMap;
    }


    private static WritableMap refreshTokenToWritableMap(RefreshTokenVo refreshTokenVo) {

        WritableMap writableMap = Arguments.createMap();
        writableMap.putDouble("userId", refreshTokenVo.getUserId());
        writableMap.putBoolean("frozen", refreshTokenVo.isFrozen());
        writableMap.putString("token", refreshTokenVo.getToken());
        writableMap.putString("tenantId", refreshTokenVo.getTenantId());
        writableMap.putString("type", refreshTokenVo.getType());
        writableMap.putDouble("expireAt", refreshTokenVo.getExpireAt());
        writableMap.putString("refreshToken", refreshTokenVo.getRefreshToken());
        writableMap.putDouble("refreshExpireAt", refreshTokenVo.getExpireAt());
        writableMap.putBoolean("refreshTokenFlag", refreshTokenVo.isRefreshTokenFlag());
        return writableMap;
    }


    public static void authError(RefreshToken refreshToken) {

        emit(AUTHERROR, refreshTokenToWritableMap(refreshToken));
    }

    private static void emit(String eventName, Object date) {
        if (reactApplicationContext == null) {
            return;
        }
        try {
            reactApplicationContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class).emit(eventName, date);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
