package cn.ipman.rpc.core.filter;

import cn.ipman.rpc.core.util.MethodUtils;
import cn.ipman.rpc.core.util.MockUtils;
import cn.ipman.rpc.core.api.Filter;
import cn.ipman.rpc.core.api.RpcRequest;
import cn.ipman.rpc.core.api.RpcResponse;
import lombok.SneakyThrows;
import java.lang.reflect.Method;

/**
 * Mock RPC 调用和返回,用Filter实现
 *
 * @Author IpMan
 * @Date 2024/3/23 21:04
 */
@SuppressWarnings("unused")
public class MockFilter implements Filter {

    @SneakyThrows
    @Override
    public Object preFilter(RpcRequest request) {
        Class<?> service = Class.forName(request.getService());
        Method method = findMethod(service, request.getMethodSign());
        assert method != null;
        Class<?> clazz = method.getReturnType();
        return MockUtils.mock(clazz);
    }

    private Method findMethod(Class<?> service, String methodSign) {
        Method[] methods = service.getMethods();
        for (Method method : methods) {
            // 如果是本地方法,就跳过
            if (MethodUtils.checkLocalMethod(method)) {
                continue;
            }
            // 对比方法签名
            String sign = MethodUtils.methodSign(method);
            if (sign.equals(methodSign)) {
                return method;
            }
        }
        return null;
    }


    @Override
    public Object postFilter(RpcRequest request, RpcResponse<?> response, Object result) {
        return null;
    }
}
