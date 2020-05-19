package com.yryz.netty.client;

import com.yryz.netty.cmd.cmd.CommondEnum;
import com.yryz.netty.cmd.transform.Transform;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class NettyCmd {

    private static final String TAG = "YryzNettyClient";

    /**
     * 同步发送
     *
     * @param channel
     * @param commondEnum
     * @return
     */
    static boolean send(Channel channel, CommondEnum commondEnum) {


        return send(channel, Transform.enumToNettyReq(commondEnum));
    }


    /**
     * 同步发送
     *
     * @param channel
     * @param nettyReqWrapper
     * @return
     */
    static boolean send(Channel channel, NettyReqWrapper nettyReqWrapper) {

        ChannelFuture channelFuture = channel.writeAndFlush(nettyReqWrapper);
        boolean success = channelFuture.isSuccess();

        return success;
    }

    /**
     * 异步发送的封装
     *
     * @param channel
     * @param observable
     * @return
     */
    private static Observable<Boolean> sendAsync(Channel channel, Observable<NettyReqWrapper> observable) {

        return Observable.just(observable)
                .filter(observable1 -> observable1 != null)
                .flatMap((Function<Observable<NettyReqWrapper>, ObservableSource<NettyReqWrapper>>)
                        observable12 -> observable12.map(nettyReqWrapper -> nettyReqWrapper))
                .flatMap((Function<NettyReqWrapper, ObservableSource<Boolean>>)
                        nettyReqWrapper -> Observable.create(emitter -> {
                                    if (channel == null || !channel.isOpen()) {
                                        emitter.onNext(false);
                                        emitter.onComplete();
                                        return;
                                    }
                                    channel.writeAndFlush(nettyReqWrapper).addListener((ChannelFutureListener) future -> {
                                        emitter.onNext(future.isSuccess());
                                        emitter.onComplete();
                                    });
                                }
                        ))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * 异步发送消息
     *
     * @param channel
     * @param cmd
     * @return
     */
    static Observable<Boolean> sendAsync(Channel channel, int cmd) {

        return sendAsync(channel, Transform.transObservable(cmd));

    }


    /**
     * 异步发送消息
     *
     * @param channel
     * @param commondEnum
     * @return
     */
    static Observable<Boolean> sendAsync(Channel channel, CommondEnum commondEnum) {

        return sendAsync(channel, Transform.transObservable(commondEnum));

    }


    /**
     * 异步消息发送
     *
     * @param channel
     * @param data
     * @return
     */
    static Observable<Boolean> sendAsync(Channel channel, String data) {

        return sendAsync(channel, Transform.transObservable(data));

    }

    /**
     * 异步发送消息
     *
     * @param channel
     * @param nettyReqWrapper
     * @return
     */
    static Observable<Boolean> sendAsync(Channel channel, NettyReqWrapper nettyReqWrapper) {

        return sendAsync(channel, Transform.transObservable((nettyReqWrapper)));
    }


}
