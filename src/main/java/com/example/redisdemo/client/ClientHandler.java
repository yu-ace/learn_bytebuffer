package com.example.redisdemo.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.CharsetUtil;

public class ClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        //发送消息到服务端
        ctx.writeAndFlush(Unpooled.copiedBuffer("set a 111"+"\n\t", CharsetUtil.UTF_8));
        ctx.writeAndFlush(Unpooled.copiedBuffer("set c 33453"+"\n\t", CharsetUtil.UTF_8));
        ctx.writeAndFlush(Unpooled.copiedBuffer("get c"+"\n\t", CharsetUtil.UTF_8));
        ctx.writeAndFlush(Unpooled.copiedBuffer("set b 222"+"\n\t", CharsetUtil.UTF_8));
        ctx.writeAndFlush(Unpooled.copiedBuffer("set c 6"+"\n\t", CharsetUtil.UTF_8));
        ctx.writeAndFlush(Unpooled.copiedBuffer("get c"+"\n\t", CharsetUtil.UTF_8));
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //接收服务端发送过来的消息
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println("收到服务端" + ctx.channel().remoteAddress() + "的消息：" + byteBuf.toString(CharsetUtil.UTF_8));
    }
}
