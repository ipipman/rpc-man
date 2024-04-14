package cn.ipman.rpc.core.filter;

import cn.ipman.rpc.core.api.Filter;
import cn.ipman.rpc.core.api.RpcContext;
import cn.ipman.rpc.core.api.RpcRequest;
import cn.ipman.rpc.core.api.RpcResponse;

import java.util.Map;

/**
 * 处理上下文参数(隐士传参)
 *
 * @Author IpMan
 * @Date 2024/4/4 17:47
 */
public class ContextParameterFilter implements Filter {

    @Override
    public Object preFilter(RpcRequest request) {
        Map<String, String> params = RpcContext.ContextParameters.get();
        if (!params.isEmpty()) {
            request.getParams().putAll(params);
        }
        return null;
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
        RpcContext.ContextParameters.get().clear();
        return null;
    }
}
