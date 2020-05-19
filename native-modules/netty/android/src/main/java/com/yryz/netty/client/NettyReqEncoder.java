package com.yryz.netty.client;

import android.text.TextUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Copyright (c) 2017-2018 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 * <p>
 * Created on 2019/3/26 19:08
 * Created by lifan
 */
public class NettyReqEncoder extends MessageToMessageEncoder<NettyReqWrapper> {

    private final Charset charset;

    /**
     * Creates a new instance with the current system character set.
     */
    public NettyReqEncoder() {
        this(Charset.forName("UTF-8"));
    }

    /**
     * Creates a new instance with the specified character set.
     */
    public NettyReqEncoder(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, NettyReqWrapper nettyReqWrapper, List<Object> out) {
        ByteBuf byteBuf = null;
        boolean release = true;
        try {
            byte[] byteMsg = new byte[0];
            if (!TextUtils.isEmpty(nettyReqWrapper.getData())) {
                byteMsg = nettyReqWrapper.getData().getBytes(charset);
            }
            byteBuf = ctx.alloc().buffer(byteMsg.length + 4);
            byteBuf.writeInt(nettyReqWrapper.getCmd());
            if (byteMsg.length > 0) {
                byteBuf.writeBytes(byteMsg);
            }
            out.add(byteBuf);
            release = false;
        } finally {
            if (release && byteBuf != null) {
                byteBuf.release();
            }
        }

    }
}
