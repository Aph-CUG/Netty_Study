## 基于Netty的服务端、客户端Demo



### Netty服务端应用程序

下面是一个服务端启动的Demo

```java
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
                    protected void initChannel(NioSocketChannel ch){}
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
```





代码首先创建了两个NioEventLoopGroup，其中bossGroup 表示监听端口，接收新连接的线程组，workerGroup 表示处理每一个连接的数据读写线程组。

- bossGroup 接收完连接，交给workerGroup去处理。



- serverBootstrap是引导类，引导服务端的启动工作。

- serverBootstrap.group(bossGroup, workerGroup) 给引导类配置两大线程组，引导类的线程模型定型。

- .channel(NioServerSocketChannel.class) 指定服务端的IO模型为NIO，这也是Netty的优势与意义所在。

- .option(ChannelOption.*SO_BACKLOG*, 1024) 是系统用于临时存放已完成三次握手的请求队列的最大长度，如果连接建立频繁，服务器处理创建新连接较慢，则可以适当调大这个参数。

- .childOption(ChannelOption.*SO_KEEPALIVE*, true) 是否开启TCP底层心跳机制，true表示开启。

- .childOption(ChannelOption.*TCP_NODELAY*, true) 是否开启Nagle算法，true表示关闭。  如果要求高实时性，有数据就马上发送，就设置为关闭，如果需要减少发送次数，减少网络交互，就设置为开启。

- .childHandler(new ChannelInitializer<NioSocketChannel>() {
      protected void initChannel(NioSocketChannel ch){}
  });    

  给引导类创建一个**ChannelInitializer**，定义后续每个连接的数据读写。

  

  与childHandler对应的还有handler()方法，childHandler相当于IO编程中为新连接新开一个线程，而handler()方法用于指定在服务端启动过程中的一些逻辑，通常情况下用不到这个方法。

- 最后，在本地绑定一个8000端口。也可以将其抽取出一个bind方法，自动递增绑定端口，找到空闲端口并绑定。











### Netty客户端应用程序

客户端的写法与服务端类似

```java
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
                    protected void initChannel(NioSocketChannel ch){}
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
```



client处理逻辑与server类似，与bind函数对应的，调用connect方法进行连接。由于connect方法返回的是一个Future，因此这个方法是异步的，通过addListener方法可以监听连接是否成功，进而打印连接信息，这里将其抽象为一个connect方法。





## 客户端与服务端的双向通信

在initChannel()方法里给用户添加一个逻辑处理器，其作用是负责向服务端写数据。

```java
.handler(new ChannelInitializer<NioSocketChannel>() {
     protected void initChannel(NioSocketChannel ch){
         ch.pipeline().addLast(new FirstClientHandler());
      }
});
```



- ch.pipeline() 返回的是和这条连接相关的处理链，采用了[责任链模式]([责任链模式 | 菜鸟教程 (runoob.com)](https://www.runoob.com/design-pattern/chain-of-responsibility-pattern.html))。
- addLast方法添加一个逻辑处理器，其作用是在客户端建立连接成功之后，向服务端写数据。逻辑处理器的代码如下：



```java
public class FirstClientHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println(new Date() + ":客户端写出数据");

        //获取数据
        ByteBuf buffer = getByteBuf(ctx);
		//写数据
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
```



该逻辑处理器继承自ChannelInboundHandlerAdapter，覆盖了其中的channelActive方法。

通过ctx.alloc().buffer()获得二进制抽象ByteBuf，再通过buffer.writeBytes(bytes) 填充数据到ByteBuf，最后，ctx.channel().writeAndFlush(buffer) 把Buffer的数据写到服务端。



在服务端同样需要给服务端添加一个逻辑处理器

```java
public class FirstServerHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println(new Date() + ": 服务端读到数据 -> " + byteBuf.toString(Charset.forName("utf-8")));

        //回复数据到客户端
        System.out.println(new Date() + ": 服务端写出数据");
        ByteBuf out = getByteBuf(ctx);
        ctx.channel().writeAndFlush(out);

    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        byte[] bytes = "你好，欢迎访问！".getBytes(Charset.forName("UTF-8"));

        ByteBuf buffer = ctx.alloc().buffer();

        buffer.writeBytes(bytes);

        return buffer;
    }
}
```





其逻辑处理器也继承自ChannelInboundHandlerAdapter，覆盖了channelRead方法，在接收到客户端发来的数据之后被回调。

接下来，服务端使用相同的方法writeAndFlush写回。客户端可以读取其中的数据，注意到客户端也覆盖了channelRead方法，用来接收服务端发来的数据。