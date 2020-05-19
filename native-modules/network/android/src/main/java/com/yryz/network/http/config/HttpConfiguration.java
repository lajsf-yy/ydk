package com.yryz.network.http.config;


import java.util.Map;

public interface HttpConfiguration {


    /**
     * 获取公共头
     *
     * @return
     */
    Map<String, String> getPublicaHeader();

    long connectTimeout();

    long writeTimeout();

    long readTimeout();

}
