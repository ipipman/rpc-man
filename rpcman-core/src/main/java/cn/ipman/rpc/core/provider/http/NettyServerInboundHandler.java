package cn.ipman.rpc.core.provider.http;

import cn.ipman.rpc.core.provider.ProviderInvoker;
import cn.ipman.rpc.core.api.RpcRequest;
import cn.ipman.rpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketException;
import java.nio.charset.StandardCharsets;

import static io.netty.handler.codec.http.HttpHeaderNames.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaderValues.KEEP_ALIVE;
import static io.netty.handler.codec.http.HttpResponseStatus.NO_CONTENT;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

/**
 * Netty Server 请求处理器
 *
 * @Author IpMan
 * @Date 2024/3/24 16:24
 */
@Slf4j
public class NettyServerInboundHandler extends ChannelInboundHandlerAdapter {

    ProviderInvoker providerInvoker;

    public NettyServerInboundHandler(ProviderInvoker providerInvoker) {
        this.providerInvoker = providerInvoker;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        FullHttpResponse response = null;
        FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
        ByteBuf buf = fullHttpRequest.content();
        try {
            String uri = fullHttpRequest.uri();

            if ("/rpcman".equals(uri)) {
                // 获取body, 转换成RpcRequest
                String requestBody = buf.toString(CharsetUtil.UTF_8);
                RpcRequest rpcRequest = JSON.parseObject(requestBody, RpcRequest.class);
                if (rpcRequest != null) {
                    // Provider Invoker
                    log.debug("netty server 接收请求体: " + rpcRequest);
                    RpcResponse<Object> rpcResponse = providerInvoker.invoke(rpcRequest);
                    log.debug("netty server 返回请求体: " + rpcResponse);
                    // 添加Server的返回体
                    String result = JSON.toJSONString(rpcResponse);
                    response = new DefaultFullHttpResponse(HTTP_1_1, OK,
                            Unpooled.wrappedBuffer(result.getBytes(StandardCharsets.UTF_8)));
                    response.headers().set("Content-Type", "application/json");
                    response.headers().setInt("Content-Length", response.content().readableBytes());
                } else {
                    response = new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN);
                }
            } else {
                response = new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND);
            }
        } catch (Exception exp) {
            log.error("netty server 处理异常: ", exp);
            response = new DefaultFullHttpResponse(HTTP_1_1, NO_CONTENT);
        } finally {
            assert response != null;
            if (buf != null) {
                buf.release();
            }
            boolean keepAlive = HttpUtil.isKeepAlive(fullHttpRequest);
            if (!keepAlive) {
                ctx.write(response).addListener(ChannelFutureListener.CLOSE);
            } else {
                response.headers().set(CONNECTION, KEEP_ALIVE);
                ctx.write(response);
            }
        }
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        if (cause instanceof SocketException se) {
            if (se.getMessage().equals("Connection reset")) {
                ctx.close();
            }
        } else {
            log.error("netty server error:", cause);
            ctx.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

}
