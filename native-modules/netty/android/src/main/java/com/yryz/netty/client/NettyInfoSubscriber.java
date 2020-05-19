package com.yryz.netty.client;


import com.yryz.network.LogUtile;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * <p>
 * override the method of you want to use
 * <p>
 * 根据业务需求重写你想使用的方法
 */

public class NettyInfoSubscriber implements Observer<NettyInfo> {

    protected Disposable disposable;

    private String TAG = "NettyTcpClient";

    private NettyProcessorHandler nettyProcessorHandler;

    public NettyInfoSubscriber(NettyProcessorHandler nettyProcessorHandler) {
        this.nettyProcessorHandler = nettyProcessorHandler;
    }


    @Override
    public final void onNext(@NonNull NettyInfo nettyInfo) {


        int statusCode = nettyInfo.getStatusCode();

        if (statusCode == NettyInfo.STATUS_CONNECT_SUCCESS) {
            LogUtile.e(TAG, "##连接成功###同步结果");
        } else if (statusCode == NettyInfo.STATUS_CONNECT_FAILED) {
            LogUtile.e(TAG, "##连接失败###同步结果");
        } else if (statusCode == NettyInfo.STATUS_CONNECT_ACTIVE) {
            LogUtile.e(TAG, "##连接成功###收到服务器反馈");
        } else if (statusCode == NettyInfo.STATUS_CONNECT_INACTIVE) {
            LogUtile.e(TAG, "##连接丢失###收到服务器反馈");
        } else if (statusCode == NettyInfo.STATUS_CONNECT_ERROR) {
            LogUtile.e(TAG, "##连接异常");
        } else if (statusCode == NettyInfo.STATUS_CONNECT_RECEIVE_MESSAGE) {
            LogUtile.e(TAG, "##连接收到消息");
            onMessage(nettyInfo.getNettyReqWrapper());
        }

    }

    private void onMessage(@NonNull NettyReqWrapper nettyReqWrapper) {
        nettyProcessorHandler.dispatchMessage(nettyReqWrapper);
    }

    @Override
    public void onSubscribe(Disposable disposable) {
        this.disposable = disposable;
    }


    public final void dispose() {
        if (disposable != null) {
            disposable.dispose();
        }
    }

    @Override
    public final void onComplete() {

    }

    @Override
    public void onError(Throwable e) {
        if (e != null) {
            LogUtile.e(TAG, "NettyInfoSubscriber onError  >>> " + e.getMessage());
        }
        nettyProcessorHandler.handleError(e);
    }

}
