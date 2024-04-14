package cn.ipman.rpc.core.consumer;

import cn.ipman.rpc.core.api.RpcRequest;
import cn.ipman.rpc.core.api.RpcResponse;

/**
 * Interface for http invoke.
 *
 * @Author IpMan
 * @Date 2024/3/23 12:19
 */
public interface HttpInvoker {

    RpcResponse<?> post(RpcRequest rpcRequest, String url);

}
