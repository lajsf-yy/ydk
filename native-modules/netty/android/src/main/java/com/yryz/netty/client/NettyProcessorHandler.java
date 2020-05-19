package com.yryz.netty.client;


public abstract class NettyProcessorHandler {

    /**
     * 消息的分发
     *
     * @param nettyReqWrapper
     */
    public void dispatchMessage(NettyReqWrapper nettyReqWrapper) {
        //TODO 暂时只分发RN的消息

        handleMessage(Netty.REGISTER_KEY_SOCKET_MODULE, nettyReqWrapper);
    }

    public abstract void handleMessage(String observableKey, NettyReqWrapper nettyReqWrapper);

    public abstract void handleError(Throwable throwable);

}
