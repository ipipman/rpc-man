package cn.ipman.rpc.core.filter;

import cn.ipman.rpc.core.api.Filter;
import cn.ipman.rpc.core.api.RpcRequest;
import cn.ipman.rpc.core.api.RpcResponse;
import org.springframework.core.annotation.Order;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/23 20:27
 */
@SuppressWarnings("all")
@Order(Integer.MAX_VALUE)
public class CacheFilter implements Filter {

    static Map<String, Object> cache = new ConcurrentHashMap<>();

    @Override
    public Object preFilter(RpcRequest request) {
        return cache.get(request.toString());
    }

    @Override
    public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
        cache.putIfAbsent(request.toString(), result);
        return result;
    }
}
