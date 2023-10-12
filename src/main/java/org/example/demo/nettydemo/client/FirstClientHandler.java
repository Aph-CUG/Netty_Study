package org.example.demo.nettydemo.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.Charset;
import java.util.Date;

public class FirstClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println(new Date() + ":客户端写出数据");

        //获取数据
        ByteBuf buffer = getByteBuf(ctx);

        ctx.channel().writeAndFlush(buffer);
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        //获取二进制抽象ByteBuf
        ByteBuf buffer = ctx.alloc().buffer();

        //准备数据，指定好字符串的字符集为UTF-8
        byte[] bytes = "Hello, world!".getBytes(Charset.forName("utf-8"));

        //填充数据到ByteBuf
        buffer.writeBytes(bytes);

        return buffer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;

        System.out.println(new Date() + ": 客户端读到数据 -> " + byteBuf.toString(Charset.forName("utf-8")));
    }
}
