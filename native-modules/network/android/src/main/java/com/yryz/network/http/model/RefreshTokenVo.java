package com.yryz.network.http.model;

import java.io.Serializable;

public class RefreshTokenVo implements Serializable {

    private long userId;
    private boolean frozen;
    private String token;
    private String tenantId;
    private String type;
    private long expireAt;
    private String refreshToken;
    private long refreshExpireAt;
    private boolean refreshTokenFlag;

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(long expireAt) {
        this.expireAt = expireAt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public long getRefreshExpireAt() {
        return refreshExpireAt;
    }

    public void setRefreshExpireAt(long refreshExpireAt) {
        this.refreshExpireAt = refreshExpireAt;
    }

    public boolean isRefreshTokenFlag() {
        return refreshTokenFlag;
    }

    public void setRefreshTokenFlag(boolean refreshTokenFlag) {
        this.refreshTokenFlag = refreshTokenFlag;
    }
}
