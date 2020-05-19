package com.yryz.netty.client;


import java.io.Serializable;

/**
 * Copyright (c) 2017-2018 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 * <p>
 * Created on 2019/3/26 19:09
 * Created by lifan
 */
public class NettyReqWrapper implements Serializable {

    private int cmd;

    private String data;

    public NettyReqWrapper() {

    }

    public NettyReqWrapper(int cmd, String data) {
        this.cmd = cmd;
        this.data = data;
    }

    public int getCmd() {
        return cmd;
    }

    public void setCmd(int cmd) {
        this.cmd = cmd;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "NettyReqWrapper{" +
                "cmd=" + cmd +
                ", data='" + data + '\'' +
                '}';
    }
}
