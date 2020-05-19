package com.yryz.netty.cmd.transform;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.yryz.network.NetworkConfig;

import com.yryz.netty.client.NettyReqWrapper;
import com.yryz.netty.cmd.cmd.BaseCmdReq;
import com.yryz.netty.cmd.cmd.CommondEnum;
import com.yryz.netty.cmd.cmd.LoginCmdReq;
import com.yryz.network.http.token.HttpHeader;
import com.yryz.network.http.token.TokenCache;


import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;
import ydk.core.YdkConfigManager;

public class Transform {
    private static NetworkConfig networkConfig ;

    private static Gson mGson;


    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        mGson = gsonBuilder.create();
        networkConfig = YdkConfigManager.getConfig(NetworkConfig.class);
    }

    /**
     * cmd 命令转 NettyReqWrapper
     *
     * @param cmd
     * @return
     */
    public static NettyReqWrapper intToToNettyReq(int cmd) {

        if (cmd == CommondEnum.CMD_LOGIN_SERVER.getValue()) {
            return enumToNettyReq(CommondEnum.CMD_LOGIN_SERVER);
        }

        return new NettyReqWrapper(cmd, mGson.toJson(new BaseCmdReq(cmd)));
    }


    /**
     * 命令转 NettyReqWrapper
     *
     * @param commondEnum
     * @return
     */
    public static NettyReqWrapper enumToNettyReq(CommondEnum commondEnum) {

        BaseCmdReq baseCmdReq = new BaseCmdReq(commondEnum.getValue());

        if (commondEnum == CommondEnum.CMD_LOGIN_SERVER) {

            HttpHeader httpHeader = TokenCache.Companion.getHttpHeader();
            LoginCmdReq loginCmdReq = new LoginCmdReq(
                    baseCmdReq.getCmd(), Long.valueOf(httpHeader.getUserId())
                    , httpHeader.getToken(),
                    networkConfig.getAppVersion(), networkConfig.getName(), 2
            );

            return new NettyReqWrapper(commondEnum.getValue(), mGson.toJson(loginCmdReq));
        }

        return new NettyReqWrapper(commondEnum.getValue(), mGson.toJson(baseCmdReq));
    }

    /**
     * @param json
     * @return
     */
    public static NettyReqWrapper jsonToNettyReq(String json) {

        return mGson.fromJson(json, NettyReqWrapper.class);

    }

    /**
     * int 命令转 NettyReqWrapper
     *
     * @param cmd
     * @return
     */
    public static Observable<NettyReqWrapper> transObservable(int cmd) {
        return transObservable(
                Observable.just(cmd)
                        .map(cmds -> intToToNettyReq(cmd)).filter(nettyReqWrapper -> nettyReqWrapper != null)

        );
    }


    /**
     * 枚举转 NettyReqWrapper
     *
     * @param commondEnum
     * @return
     */
    public static Observable<NettyReqWrapper> transObservable(CommondEnum commondEnum) {

        return transObservable(Observable.just(commondEnum)
                .filter(commondEnum1 -> commondEnum1 != null)
                .map(commondEnum12 -> enumToNettyReq(commondEnum12))
                .filter(nettyReqWrapper -> nettyReqWrapper != null));

    }

    /**
     * json 转
     *
     * @param json
     * @return
     */
    public static Observable<NettyReqWrapper> transObservable(String json) {

        return transObservable(Observable.just(json)
                .filter(msg -> !TextUtils.isEmpty(msg))
                .map(s -> jsonToNettyReq(s))
                .filter(nettyReqWrapper -> nettyReqWrapper != null));

    }

    /**
     * nettyReqWrapper
     *
     * @param nettyReqWrapper
     * @return
     */
    public static Observable<NettyReqWrapper> transObservable(NettyReqWrapper nettyReqWrapper) {

        return transObservable(Observable.just(nettyReqWrapper)
                .filter(wrapper -> wrapper != null));
    }

    /**
     * 校验参数的合法性
     *
     * @param observable
     * @return
     */
    private static Observable<NettyReqWrapper> transObservable(Observable<NettyReqWrapper> observable) {

        return Observable.just(observable)
                .filter(observable1 -> observable1 != null)
                .flatMap((Function<Observable<NettyReqWrapper>, ObservableSource<NettyReqWrapper>>)
                        observable12 ->
                                observable12.map(nettyReqWrapper -> nettyReqWrapper))
                .filter(nettyReqWrapper -> nettyReqWrapper != null)
                .map(nettyReqWrapper -> {
                    String data = nettyReqWrapper.getData();
                    JsonObject jsonObject = mGson.fromJson(data, JsonObject.class);
                    if (!jsonObject.has("cmd")) {
                        jsonObject.addProperty("cmd", nettyReqWrapper.getCmd());
                    }
                    nettyReqWrapper.setData(jsonObject.toString());
                    return nettyReqWrapper;
                });
    }


    public static <T> Observable<T> transform(String josn, Class<T> tClass) {
        return Observable.just(josn)
                .filter(s -> !TextUtils.isEmpty(s))
                .map(s -> mGson.fromJson(s, tClass));

    }

}
