package cn.ipman.rpc.core.provider.http;

import cn.ipman.rpc.core.provider.ProviderInvoker;
import cn.ipman.rpc.core.api.RpcException;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;

/**
 * Netty Server BootStrap
 *
 * @Author IpMan
 * @Date 2024/3/24 16:01
 */
@Slf4j
public class NettyServer {


    int port;

    ProviderInvoker providerInvoker;

    public NettyServer(int port, ProviderInvoker providerInvoker) {
        this.port = port;
        System.out.println("====> netty port = " + port);
        this.providerInvoker = providerInvoker;
    }

    public void start() throws Throwable {
        new Thread(() -> {
            try {
                // 异步启动
                runNettyThread();
            } catch (Throwable e) {
                throw new RpcException(e);
            }
        }).start();
    }

    public void runNettyThread() throws Throwable {
        EventLoopGroup boosGroup = new NioEventLoopGroup(5);
        EventLoopGroup workerGroup = new NioEventLoopGroup(1000);
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.option(ChannelOption.SO_BACKLOG, 512) // 连接队列大小
                    .option(ChannelOption.TCP_NODELAY, true) // 关闭Nagle,即时传输
                    .option(ChannelOption.SO_KEEPALIVE, true) // 支持长连接
                    .option(ChannelOption.SO_REUSEADDR, true) // 共享端口
                    .option(ChannelOption.SO_RCVBUF, 32 * 1024) // 操作缓冲区的大小
                    .option(ChannelOption.SO_SNDBUF, 32 * 1024) // 发送缓冲区的大小
                    .option(EpollChannelOption.SO_REUSEPORT, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true);

            b.group(boosGroup, workerGroup).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline p = ch.pipeline();
                            p.addLast(new HttpServerCodec()); // request/response HTTP编解码
                            p.addLast(new HttpObjectAggregator(10 * 1024 * 1024)); // 传输内容最大长度
                            p.addLast(new NettyServerInboundHandler(providerInvoker)); // 请求处理器
                        }
                    });
            String ip = InetAddress.getLocalHost().getHostAddress();
            Channel ch = b.bind(ip, port).sync().channel();
            log.info("open netty http server，listener form http://" + ip + ":" + port + '/');
            ch.closeFuture().sync();
        } finally {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            log.info("netty server console..");
        }
    }


}
