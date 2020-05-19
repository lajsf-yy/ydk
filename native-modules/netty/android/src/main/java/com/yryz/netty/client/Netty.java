package com.yryz.netty.client;




import io.reactivex.Observable;

public class Netty {

    public static final String REGISTER_KEY_SOCKET_MODULE = "SocketModule";



    public static void init() {

    }

    /**
     * 启动
     */
    public static void start() {

        NettyTcpClient.getInstance().start();
    }

    public static void stop() {
        NettyTcpClient.getInstance().stop();
    }

    /**
     * 异步消息发送发送
     *
     * @param nettyReqWrapper
     * @return
     */
    public static Observable<Boolean> sendMessage(NettyReqWrapper nettyReqWrapper) {
        return NettyTcpClient.getInstance().sendMessage(nettyReqWrapper);
    }

    /**
     * 异步消息发送发送
     *
     * @param msg
     * @return
     */
    public static Observable<Boolean> sendMessage(String msg) {

        return NettyTcpClient.getInstance().sendMessage(msg);
    }


    /**
     * RN 模块注册
     *
     * @return
     */
    public static Observable<NettyReqWrapper> registerRNObservable() {

        return NettyTcpClient.getInstance().registerObservable(REGISTER_KEY_SOCKET_MODULE);

    }
}
