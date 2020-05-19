package com.yryz.netty.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Copyright (c) 2017-2018 Wuhan Yryz Network Company LTD.
 * All rights reserved.
 * <p>
 * Created on 2019/3/26 19:25
 * Created by lifan
 */
public class NettyReqDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final Charset charset;

    /**
     * Creates a new instance with the current system character set.
     */
    public NettyReqDecoder() {
        this(Charset.forName("UTF-8"));
    }

    /**
     * Creates a new instance with the specified character set.
     */
    public NettyReqDecoder(Charset charset) {
        if (charset == null) {
            throw new NullPointerException("charset");
        }
        this.charset = charset;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        NettyReqWrapper nettyReqWrapper = new NettyReqWrapper();
        int len = in.readableBytes();
        if (len < 4) {
            in.skipBytes(len);
            throw new RuntimeException("NettyReqDecoder ByteBuf readableBytes is less than 4");
        }
        int cmd = in.getInt(0);
        nettyReqWrapper.setCmd(cmd);
        in.skipBytes(4);
        nettyReqWrapper.setData(in.toString(charset));
        out.add(nettyReqWrapper);
    }
}
