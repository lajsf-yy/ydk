package com.yryz.netty.client;

import java.io.Serializable;

public class NettyInfo implements Serializable {

    public static int STATUS_CONNECT_SUCCESS = 2000;

    public static int STATUS_CONNECT_FAILED = 2001;

    public static int STATUS_CONNECT_ACTIVE = 2002;

    public static int STATUS_CONNECT_INACTIVE = 2003;

    public static int STATUS_CONNECT_ERROR = 2004;

    public static int STATUS_CONNECT_RECEIVE_MESSAGE = 2005;

    public static int STATUS_LOGIN_SUCCESS = 2006;


    private int statusCode;

    private NettyReqWrapper nettyReqWrapper;


    public NettyInfo(int statusCode) {
        this(statusCode, new NettyReqWrapper());
    }

    public NettyInfo(int statusCode, NettyReqWrapper nettyReqWrapper) {
        this.statusCode = statusCode;
        this.nettyReqWrapper = nettyReqWrapper;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public NettyReqWrapper getNettyReqWrapper() {

        return nettyReqWrapper;
    }
}
