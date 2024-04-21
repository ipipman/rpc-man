package cn.ipman.rpc.core.consumer.http;

import cn.ipman.rpc.core.api.RpcException;
import cn.ipman.rpc.core.api.RpcRequest;
import cn.ipman.rpc.core.api.RpcResponse;
import cn.ipman.rpc.core.consumer.HttpInvoker;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/31 15:40
 */
@Slf4j
public class NettyClient implements HttpInvoker {

    int timeout;

    public NettyClient(int timeout) {
        this.timeout = timeout;
    }

    @Override
    @SuppressWarnings("all")
    public RpcResponse<?> post(RpcRequest rpcRequest, String url) {
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new LoggingHandler(LogLevel.INFO));
            b.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, timeout); // 连接超时

            NettyClientInboundHandler clientInboundHandler = new NettyClientInboundHandler();
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline p = ch.pipeline();
                    p.addLast(new ReadTimeoutHandler(timeout, TimeUnit.MILLISECONDS)); // 读超时
                    p.addLast(new HttpClientCodec()); // request/response HTTP编解码
                    p.addLast(new HttpObjectAggregator(10 * 1024 * 1024)); // 传输内容最大长度
                    p.addLast(clientInboundHandler); // 请求处理器
                }
            });

            // Start the client.
            URI uri = new URI(url);
            String host = uri.getHost();
            FullHttpRequest request = new DefaultFullHttpRequest(
                    HttpVersion.HTTP_1_1, HttpMethod.POST, uri.getRawPath());

            // 构建HTTP请求体
            request.headers().set(HttpHeaderNames.HOST, host);
            request.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            request.headers().set(HttpHeaderNames.ACCEPT_ENCODING, HttpHeaderValues.GZIP);
            request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");

            // body
            ByteBuf buf = Unpooled.copiedBuffer(JSON.toJSONString(rpcRequest), StandardCharsets.UTF_8);
            request.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
            request.content().clear().writeBytes(buf);

            // 发送http请求
            Channel channel = b.connect(uri.getHost(), uri.getPort()).sync().channel();
            channel.writeAndFlush(request).sync();
            channel.closeFuture().sync();

            if (clientInboundHandler.getException() != null) {
                throw new RpcException(clientInboundHandler.getException());
            }
            return clientInboundHandler.getRpcResponse();
        } catch (RpcException e) {
            throw e;
        } catch (Exception e) {
            throw new RpcException(e);
        } finally {
            workerGroup.shutdownGracefully();
        }
    }

    @Override
    public String post(String requestString, String url) {
        throw new UnsupportedOperationException("Currently in netty implementation, it is not supported yet");
    }

    @Override
    public String get(String url) {
        throw new UnsupportedOperationException("Currently in netty implementation, it is not supported yet");
    }

    public static void main(String[] args) {
        RpcRequest request = new RpcRequest();
        request.setService("cn.ipman.rpc.demo.api.UserService");
        request.setArgs(new Object[]{1});
        request.setMethodSign("findById@1_int");
        // client to...
        NettyClient client = new NettyClient(10);
        RpcResponse<?> response = client.post(request, "http://192.168.31.232:9081/");
        System.out.println(response);

    }
}
