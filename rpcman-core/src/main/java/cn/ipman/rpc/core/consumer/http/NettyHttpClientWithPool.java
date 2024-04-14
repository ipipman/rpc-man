package cn.ipman.rpc.core.consumer.http;

import cn.ipman.rpc.core.api.RpcRequest;
import com.alibaba.fastjson.JSON;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.concurrent.Future;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;

public class NettyHttpClientWithPool {

    private final FixedChannelPool pool;

    public NettyHttpClientWithPool(String host, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.group(new NioEventLoopGroup());
        bootstrap.remoteAddress(host, port);
        bootstrap.handler(new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel ch) {
                ch.pipeline().addLast(new HttpClientCodec());
                ch.pipeline().addLast(new HttpObjectAggregator(1024 * 1024));
                ch.pipeline().addLast(new SimpleChannelInboundHandler<HttpObject>() {
                    @Override
                    protected void channelRead0(ChannelHandlerContext ctx, HttpObject msg) {
                        if (msg instanceof FullHttpResponse) {
                            FullHttpResponse response = (FullHttpResponse) msg;
                            System.out.println("Response received: " + response.content().toString(CharsetUtil.UTF_8));
                            // Handle the response
                            ctx.close(); // Close the connection after handling the response
                        }
                    }
                });
            }
        });

        pool = new FixedChannelPool(bootstrap, new ChannelPoolHandler() {
            @Override
            public void channelReleased(Channel ch) {
                System.out.println("Channel released: " + ch);
            }

            @Override
            public void channelAcquired(Channel ch) {
                System.out.println("Channel acquired: " + ch);
            }

            @Override
            public void channelCreated(Channel ch) {
                System.out.println("Channel created: " + ch);
            }
        }, 10); // Pool size
    }

    public void post(String uri, String content) throws URISyntaxException {
        URI uriObject = new URI(uri);
        String host = uriObject.getHost();
        int port = uriObject.getPort();

//        DefaultFullHttpRequest request = new DefaultFullHttpRequest(
//                HttpVersion.HTTP_1_1, HttpMethod.POST, uriObject.getRawPath(),
//                Unpooled.copiedBuffer(content, CharsetUtil.UTF_8));

        FullHttpRequest request = new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.POST, uriObject.getRawPath());


        request.headers().set(HttpHeaderNames.HOST, host);
        request.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
//        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, request.content().readableBytes());

        // body
        ByteBuf buf = Unpooled.copiedBuffer(content, StandardCharsets.UTF_8);
        request.headers().set(HttpHeaderNames.CONTENT_LENGTH, buf.readableBytes());
        request.content().clear().writeBytes(buf);


        pool.acquire().addListener((Future<Channel> future) -> {
            if (future.isSuccess()) {
                Channel channel = future.getNow();

                channel.writeAndFlush(request).addListener((ChannelFutureListener) channelFuture -> {
                    if (channelFuture.isSuccess()) {
                        System.out.println("Request sent successfully");
                    } else {
                        System.err.println("Failed to send request: " + channelFuture.cause());
                    }
                    // Release the channel back to the pool
                    pool.release(channel);
                });
            } else {
                System.err.println("Failed to acquire a channel: " + future.cause());
            }
        });
    }

    public static void main(String[] args) throws URISyntaxException {

        RpcRequest request = new RpcRequest();
        request.setService("cn.ipman.rpc.demo.api.UserService");
        request.setArgs(new Object[]{1});
        request.setMethodSign("findById@1_int");

        NettyHttpClientWithPool client = new NettyHttpClientWithPool("192.168.31.232", 9081);
        client.post("http://192.168.31.232:9081/", JSON.toJSONString(request));
    }
}