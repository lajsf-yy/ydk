package com.yryz.netty.client;


import com.yryz.network.LogUtile;
import com.yryz.netty.cmd.cmd.CommondEnum;
import com.yryz.netty.cmd.cmd.LoginCmdResult;
import com.yryz.netty.cmd.transform.Transform;
import com.yryz.network.http.token.TokenEnum;
import com.yryz.network.http.token.TokenIllegalStateException;


import java.net.ConnectException;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.reactivex.ObservableEmitter;


public class NettyClientHandler extends SimpleChannelInboundHandler<NettyReqWrapper> {

    private static final String TAG = "YryzNettyClient";

    private ObservableEmitter<NettyInfo> emitter;

    private volatile int heartbeat;


    public NettyClientHandler(ObservableEmitter<NettyInfo> emitter) {
        this.emitter = emitter;

    }


    /**
     * 心跳处理
     *
     * @param ctx
     * @param evt
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            //发送心跳
            if (event.state() == IdleState.WRITER_IDLE) {
                heartbeat++;
                NettyCmd.sendAsync(ctx.channel(), CommondEnum.CMD_HEARTBEAT).subscribe(aBoolean -> {
                    //  LogUtile.e(TAG, "心跳处理发送结果 ### " + aBoolean);
                });
            }
        }
        if (heartbeat > 5) {
            emitter.onError(new ConnectException("heartbeat no respond"));
        }
    }


    /**
     * 连接成功
     *
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {

        NettyCmd.send(ctx.channel(), CommondEnum.CMD_LOGIN_SERVER);

        if (!emitter.isDisposed()) {
            emitter.onNext(new NettyInfo(NettyInfo.STATUS_CONNECT_ACTIVE));
        }

    }


    /**
     * 断开连接
     *
     * @param ctx
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        LogUtile.e(TAG, "断开连接 调用channelInactive");
        if (!emitter.isDisposed()) {
            emitter.onNext(new NettyInfo(NettyInfo.STATUS_CONNECT_INACTIVE));
            emitter.onError(new ConnectException("channelInactive"));
        }
    }

    /**
     * 异常处理
     *
     * @param ctx
     * @param cause
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

        ctx.channel().close();
        if (!emitter.isDisposed()) {
            emitter.onNext(new NettyInfo(NettyInfo.STATUS_CONNECT_ERROR));
        }

    }

    /**
     * 收到消息
     *
     * @param ctx
     * @param nettyReqWrapper
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, NettyReqWrapper nettyReqWrapper) throws Exception {

        //   LogUtile.e(TAG, "收到消息 " + nettyReqWrapper.toString());
        //心跳回执
        if (nettyReqWrapper.getCmd() == CommondEnum.CMD_HEARTBEAT_RECEIPT.getValue()) {
            //只要有心跳，就重置
            heartbeat = 0;
        }

        //负数回执（服务端发送给客户端）
        if (nettyReqWrapper.getCmd() < 0) {
            //TODO
            return;
        }
        //正数消息回执（客户端发送给服务端）
        if (nettyReqWrapper.getCmd() > 0) {
            NettyCmd.sendAsync(ctx.channel(), nettyReqWrapper.getCmd() * -1).subscribe(aBoolean -> {

            });
        }
        if (nettyReqWrapper.getCmd() == CommondEnum.CMD_LOGIN_AUTHORIZATION.getValue()) {
            loginProcessor(nettyReqWrapper);

            return;
        }
        if (!emitter.isDisposed()) {
            emitter.onNext(new NettyInfo(NettyInfo.STATUS_CONNECT_RECEIVE_MESSAGE, nettyReqWrapper));
        }

    }

    /**
     * @param nettyReqWrapper
     */
    private void loginProcessor(NettyReqWrapper nettyReqWrapper) {
        Transform.transform(nettyReqWrapper.getData(), LoginCmdResult.class).subscribe(loginCmdResult -> {
            int code = Integer.valueOf(loginCmdResult.getCode());
            //  code =101;
            if (TokenEnum.CODE_200.getCode() == code) {
                LogUtile.e(TAG, "TCP 登录成功");
                emitter.onNext(new NettyInfo(NettyInfo.STATUS_LOGIN_SUCCESS));
            } else {
                emitter.onError(new TokenIllegalStateException(code, loginCmdResult.getMsg()));
            }

        }, throwable -> {
            emitter.onError(throwable);
        });
    }

}
