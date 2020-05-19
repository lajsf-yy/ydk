package com.yryz.netty.client;

import android.os.Looper;
import android.support.annotation.NonNull;

import com.yryz.network.LogUtile;
import com.yryz.network.NetworkConfig;
import com.yryz.network.YdkNetwork;
import com.yryz.network.http.network.NeetWork;
import com.yryz.network.http.network.NetWorkChangListener;
import com.yryz.network.http.token.TokenController;
import com.yryz.network.http.token.TokenIllegalStateException;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.LengthFieldPrepender;
import io.netty.handler.timeout.IdleStateHandler;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import ydk.core.YdkConfigManager;


/**
 * TCP 客户端
 */
public class NettyTcpClient {

    private  NetworkConfig networkConfig =YdkConfigManager.getConfig(NetworkConfig.class);
    private ThreadLocal<Boolean> mThreadLocal = new ThreadLocal<>();

    private static final String TAG = "YryzNettyClient";

    private EventLoopGroup group;

    private Channel channel;
    /**
     * 重连次数
     */
    private volatile int tryCount = 0;
    /**
     * 最大重连次数
     */
    private int mxTryCount = 6;
    /**
     * 重连时间
     */
    private long interval = 10;

    private static NettyTcpClient instance;


    private Map<String, ObservableEmitter<NettyReqWrapper>> observableEmitters = new HashMap<>();


    private NettyProcessorHandler nettyProcessorHandler = new NettyProcessorHandler() {

        @Override
        public void handleMessage(String observableKey, NettyReqWrapper nettyReqWrapper) {
            ObservableEmitter<NettyReqWrapper> observableEmitter = observableEmitters.get(observableKey);
            if (observableEmitter == null || observableEmitter.isDisposed()) {
                observableEmitters.remove(observableKey);
                return;
            }
            observableEmitter.onNext(nettyReqWrapper);

        }

        @Override
        public void handleError(Throwable throwable) {
            error();
            if (throwable instanceof TokenIllegalStateException) {
                TokenIllegalStateException tokenIllegalStateException = (TokenIllegalStateException) throwable;
                handlerToken(tokenIllegalStateException.getCode());
            }
        }
    };


    private NetWorkChangListener mNetWorkChangListener = new NetWorkChangListener() {
        @Override
        public void onNetWorkChang(boolean link) {
            //网诺链接，并且在close 状态下，才会重连
            if (link && close) {
                start();
            }

        }
    };

    /**
     * 处理token
     *
     * @param code
     */
    private void handlerToken(int code) {

        TokenController.Companion.handlerToken(code).subscribe(aBoolean -> {
            if (aBoolean) {
                //3 秒后重启
                Observable.timer(3, TimeUnit.SECONDS)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(aLong -> {
                            mThreadLocal.set(false);
                            start();
                        });
            }
        }, throwable -> {
            if (throwable != null) {
                LogUtile.e(TAG, "renewToken throwable " + throwable.getMessage());
            }
        });
    }


    private NettyTcpClient() {

    }

    static NettyTcpClient getInstance() {
        if (instance == null) {
            synchronized (NettyTcpClient.class) {
                if (instance == null) {
                    instance = new NettyTcpClient();
                }
            }
        }
        return instance;
    }

    /**
     * 注册监听
     *
     * @return
     */
    Observable<NettyReqWrapper> registerObservable(String key) {

        return Observable.create(emitter -> {
            if (observableEmitters.containsKey(key)) {
                ObservableEmitter<NettyReqWrapper> nettyReqWrapperObservableEmitter = observableEmitters.get(key);
                if (nettyReqWrapperObservableEmitter != null) {
                    nettyReqWrapperObservableEmitter.onComplete();
                    observableEmitters.remove(key);
                }
            }
            observableEmitters.put(key, emitter);
        });
    }


    /**
     * 发送消息
     *
     * @param data
     * @return
     */
    Observable<Boolean> sendMessage(String data) {

        return NettyCmd.sendAsync(channel, data);
    }

    /**
     * 发送消息
     *
     * @param reqWrapper
     * @return
     */
    Observable<Boolean> sendMessage(NettyReqWrapper reqWrapper) {

        return NettyCmd.sendAsync(channel, reqWrapper);
    }


    void start() {


        String host = networkConfig.getSocketHost();

        int port = Integer.valueOf(networkConfig.getSocketPort());

        if (Looper.myLooper() != Looper.getMainLooper()) {
            throw new RuntimeException("must start netty in main thread ");
        }
        if (mThreadLocal.get() == null) {
            mThreadLocal.set(false);
        }
        if (mThreadLocal.get()) {
            return;
        }
        NeetWork.registerListener(TAG, mNetWorkChangListener);
        tryCount = 0;
        close = false;

        mThreadLocal.set(true);

        Observable.create(new NettyOnSubscribe(host, port))
                .retryWhen(throwableObservable ->
                        throwableObservable.flatMap((Function<Throwable, ObservableSource<?>>) throwable -> {
                            if (throwable != null) {
                                LogUtile.e(TAG, "retry  throwable" + throwable.getClass().getName());
                            }
                            boolean retry = throwable instanceof IOException
                                    || throwable instanceof TimeoutException
                                    || throwable instanceof ConnectException
                                    || throwable instanceof InterruptedException;

                            //重连10次以上，关闭
                            if (tryCount < mxTryCount && retry && !close) {
                                LogUtile.e(TAG, "异常，retryWhen 重连延时");
                                tryCount++;
                                return Observable.timer(interval, TimeUnit.SECONDS);
                            }
                            LogUtile.e(TAG, "异常，retryWhen 抛出异常 ");
                            tryCount = 0;
                            //抛出异常
                            return Observable.error(throwable);
                        }))
                .doOnComplete(() -> {
                    channel = null;
                    group = null;
                })
                .doOnNext(nettyInfo -> {
                    if (nettyInfo.getStatusCode() == NettyInfo.STATUS_CONNECT_ACTIVE) {
                        tryCount = 0;
                    }
                })
                .share()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new NettyInfoSubscriber(nettyProcessorHandler));

    }


    private volatile boolean close;


    void stop() {
        // 断开链接
        mThreadLocal.set(false);
        close = true;

//        if (channel != null) {
//            channel.close();
//        }
        if (group != null) {
            group.shutdownGracefully();
        }
        NeetWork.unrRegisterListener(TAG);
    }

    void error() {
        mThreadLocal.set(false);
        close = true;
        if (group != null) {
            group.shutdownGracefully();
        }
        channel = null;
        group = null;

    }

    // private volatile boolean isNeedReconnect;

    private final class NettyOnSubscribe implements ObservableOnSubscribe<NettyInfo> {

        private String host;

        private int port;


        public NettyOnSubscribe(String host, int port) {
            this.host = host;
            this.port = port;
        }

        @Override
        public void subscribe(@NonNull ObservableEmitter<NettyInfo> emitter) throws Exception {
            LogUtile.e(TAG, "开始执行subscribe");
            if (close) {
                emitter.onComplete();
                return;
            }
            LogUtile.e(TAG, "开始执行initNetty");
            initNetty(emitter);

        }

        private void initNetty(final ObservableEmitter<NettyInfo> emitter) throws InterruptedException {
            if (channel != null && channel.isActive()) {
                LogUtile.e(TAG, "initNetty ### Netty服务活跃");
                return;
            }
            if (group != null) {
                group.shutdownGracefully();
                channel = null;
                group = null;
            }
            LogUtile.e(TAG, "initNetty ### 初始化Netty服务");


            ChannelFuture channelFuture = null;

            group = new NioEventLoopGroup();

            emitter.setCancellable(() -> {

                if (group != null) {
                    group.shutdownGracefully();
                }

            });

            Bootstrap bootstrap = new Bootstrap().group(group)
                    // .option(ChannelOption.TCP_NODELAY, true)
                    // .option(ChannelOption.SO_KEEPALIVE, true)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {

                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
                            // 解码handler
                            // 读取指定长度的字节数据
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(
                                    100 * 1024 * 1024, 0, 4, 0, 4));
                            // 将ByteBuf解码为String
                            pipeline.addLast(new NettyReqDecoder(Charset.forName("UTF-8")));
                            // 编码handler
                            // 编码时在协议头增加协议体长度
                            pipeline.addLast(new LengthFieldPrepender(4));
                            // 将String编码为ByteBuf
                            pipeline.addLast(new NettyReqEncoder(Charset.forName("UTF-8")));
                            // 业务逻辑Handler
                            pipeline.addLast("BusinessHandler", new NettyClientHandler(emitter));

                        }
                    });

            channelFuture = bootstrap.connect(host, port)
                    .addListener((ChannelFutureListener) channelFuture1 -> {
                        if (channelFuture1.isSuccess()) {
                            //  LogUtile.e(TAG, "连接成功###同步结果");
                            if (!emitter.isDisposed()) {
                                emitter.onNext(new NettyInfo(NettyInfo.STATUS_CONNECT_SUCCESS));
                            }
                        } else {
                            // LogUtile.e(TAG, "连接失败###同步结果");
                            if (!emitter.isDisposed()) {
                                emitter.onNext(new NettyInfo(NettyInfo.STATUS_CONNECT_FAILED));
                            }
                        }
                    })
                    .sync();
            channel = channelFuture.channel();
            try {
                channel.closeFuture().sync();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            LogUtile.e(TAG, "连接断开，线程中断结束");

        }
    }


}
