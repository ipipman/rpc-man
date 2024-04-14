package cn.ipman.rpc.core.consumer.http;

import cn.ipman.rpc.core.api.RpcResponse;
import com.alibaba.fastjson.JSON;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import io.netty.handler.timeout.ReadTimeoutException;
import io.netty.util.CharsetUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;


/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/31 15:57
 */
@Getter
@Slf4j
public class NettyClientInboundHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    private RpcResponse<?> rpcResponse;
    private Exception exception;


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpResponse fullHttpResponse) {
        ByteBuf buf = fullHttpResponse.content();
        log.debug("netty client http response={}", buf.toString(CharsetUtil.UTF_8));
        rpcResponse = JSON.parseObject(buf.toString(CharsetUtil.UTF_8), RpcResponse.class);
        log.debug("netty client rpcResponse={}", rpcResponse);
        ctx.flush();
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 拿到超时异常
        if (cause instanceof ReadTimeoutException exp) {
            exception = exp;
        } else {
            super.exceptionCaught(ctx, cause);
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }
}
