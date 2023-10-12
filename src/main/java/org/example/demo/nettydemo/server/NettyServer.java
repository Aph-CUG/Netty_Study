package org.example.demo.nettydemo.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyServer {
    private static final int PORT = 8000;
    public static void main(String[] args) {
        //bossGroup 表示监听端口，接收新连接的线程组
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        //workerGroup 表示处理每一个连接的数据读写线程组
        // bossGroup 接收完连接，交给workerGroup去处理
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        //引导类，引导服务端的启动工作
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        //给引导类配置两大线程组，引导类的线程模型定型
        serverBootstrap.group(bossGroup, workerGroup)
                //指定服务端的IO模型为NIO
                .channel(NioServerSocketChannel.class)
                //系统用于临时存放已完成三次握手的请求队列的最大长度，如果连接建立频繁，
                // 服务器处理创建新连接较慢，则可以适当调大这个参数
                .option(ChannelOption.SO_BACKLOG, 1024)
                //是否开启TCP底层心跳机制，true表示开启
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                //是否开启Nagle算法，true表示关闭
                .childOption(ChannelOption.TCP_NODELAY, true)
                //给引导类创建一个ChannelInitializer，定义后续每个连接的数据读写
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch){
                        ch.pipeline().addLast(new FirstServerHandler());
                    }
                });
        //serverBootstrap.bind(8000);
        bind(serverBootstrap, PORT);
    }

    //自动绑定递增端口
    private static void bind(final ServerBootstrap serverBootstrap,final int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if(future.isSuccess()) {
                System.out.println("端口[" + port + "]绑定成功！");
            } else {
                System.out.println("端口[" + port + "]绑定失败！");
                //端口号自增
                bind(serverBootstrap, port + 1);
            }
        });

    }
}
