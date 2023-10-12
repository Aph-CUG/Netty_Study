package org.example.demo.nettydemo.client;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class NettyClient {
    private static final int MAX_RETRY = 5;
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 8000;
    public static void main(String[] args) {

        //workerGroup 表示处理每一个连接的数据读写线程组
        // bossGroup 接收完连接，交给workerGroup去处理
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        //引导类，引导服务端的启动工作
        Bootstrap bootstrap = new Bootstrap();
        //给引导类配置两大线程组，引导类的线程模型定型
        bootstrap.group(workerGroup)
                //指定服务端的IO模型为NIO
                .channel(NioServerSocketChannel.class)
                //连接的超时时间
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                //是否开启TCP底层心跳机制，true表示开启
                .option(ChannelOption.SO_KEEPALIVE, true)
                //是否开启Nagle算法，true表示关闭
                .option(ChannelOption.TCP_NODELAY, true)
                //给引导类创建一个ChannelInitializer，定义后续每个连接的数据读写
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch){
                        ch.pipeline().addLast(new FirstClientHandler());
                    }
                });
        //serverBootstrap.bind(8000);
        connect(bootstrap,HOST, PORT,MAX_RETRY);
    }

    private static void connect(Bootstrap bootstrap, String host, int port, int retry) {
        bootstrap.connect(host, port).addListener(future -> {
            if(future.isSuccess()) {
                System.out.println("连接成功！");
            } else if (retry == 0) {
                System.err.println("重试次数已用完，放弃连接！");
            } else {
                int order = (MAX_RETRY - retry) + 1;
                int delay = 1 << order;
                System.err.println(new Date() + ": 连接失败，第" + order + "次重连……");
                bootstrap.config().group().schedule(() -> connect(bootstrap,host,port, retry - 1),
                        delay, TimeUnit.SECONDS);
            }
        });
    }
}
