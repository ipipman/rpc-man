package cn.ipman.rpc.core.provider;

import cn.ipman.rpc.core.config.ProviderConfigProperties;
import cn.ipman.rpc.core.meta.ProviderMeta;
import cn.ipman.rpc.core.util.TypeUtils;
import cn.ipman.rpc.core.api.RpcContext;
import cn.ipman.rpc.core.api.RpcException;
import cn.ipman.rpc.core.api.RpcRequest;
import cn.ipman.rpc.core.api.RpcResponse;
import cn.ipman.rpc.core.governance.SlidingTimeWindow;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static cn.ipman.rpc.core.api.RpcException.ExceedLimitEx;


/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/23 11:52
 */
@Slf4j
public class ProviderInvoker {

    private final MultiValueMap<String, ProviderMeta> skeleton;
    final Map<String, SlidingTimeWindow> windows = new HashMap<>();
    final ProviderConfigProperties providerProperties;

    public ProviderInvoker(ProviderBootstrap providerBootstrap) {
        this.skeleton = providerBootstrap.getSkeleton();
        this.providerProperties = providerBootstrap.getProviderProperties();
    }

    public RpcResponse<Object> invoke(RpcRequest request) {
        log.debug(" ===> ProviderInvoker.invoke(request:{})", request);
        // 隐式传参
        if (!request.getParams().isEmpty()) {
            request.getParams().forEach(RpcContext::setContextParameter);
        }

        RpcResponse<Object> rpcResponse = new RpcResponse<>();
        String service = request.getService();

        int trafficControl = Integer.parseInt(
                this.providerProperties.getMetas().getOrDefault("tc", "20"));
        // todo 1201 : 改成map，针对不同的服务用不同的流控值
        // todo 1202 : 对多个节点是共享一个数值，，，把这个map放到redis

        // 添加流量控制, 默认30s内大于20次访问,被限流
        synchronized (windows) {
            log.debug(" ===>> trafficControl:{} for {}", trafficControl, service);
            SlidingTimeWindow window = windows.computeIfAbsent(service, k -> new SlidingTimeWindow());
            if (window.calcSum() >= trafficControl) {
                throw new RpcException("service " + service + " invoked in 30s/[" +
                        window.getSum() + "] larger than tpsLimit = " + trafficControl, ExceedLimitEx);
            }
            window.record(System.currentTimeMillis());
            log.debug("service {} in window with {}", service, window.getSum());
        }

        // 根据类包名,获取容器的类实例
        List<ProviderMeta> providerMetas = this.skeleton.get(service);
        try {
            String methodSign = request.getMethodSign();
            // 从元数据里获取类方法
            ProviderMeta meta = findProviderMeta(providerMetas, methodSign);
            Method method = meta.getMethod();

            // 参数类型转换
            Object[] args = processArgs(request.getArgs(), method.getParameterTypes(), method.getGenericParameterTypes());
            // 传入方法参数,通过反射 调用目标provider方法
            Object result = method.invoke(meta.getServiceImpl(), args);

            rpcResponse.setStatus(true);
            rpcResponse.setData(result);
        } catch (InvocationTargetException e) {
            // Provider反射时异常处理, TODO 返回反射目标类的异常
            rpcResponse.setEx(new RpcException(e.getTargetException().getMessage()));
        } catch (IllegalAccessException | IllegalArgumentException e) {
            // Provider反射调用和参数时异常
            rpcResponse.setEx(new RpcException(e.getMessage()));
        } catch (Exception e) {
            log.error(" ===> ProviderInvoker.invoke() unknown error:", e);
            rpcResponse.setEx(new RpcException(e.getMessage()));
        } finally {
            // 清除RpcContext中ThreadLocal的隐式参数的
            RpcContext.ContextParameters.get().clear();
        }
        log.debug(" ===> ProviderInvoker.invoke() = {}", rpcResponse);
        return rpcResponse;
    }

    private Object[] processArgs(Object[] args, Class<?>[] parameterTypes, Type[] genericParameterTypes) {
        if (args == null || args.length == 0) return args;
        // 参数类型转换
        Object[] actualArgs = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            actualArgs[i] = TypeUtils.castGeneric(args[i], parameterTypes[i], genericParameterTypes[i]);
        }
        return actualArgs;
    }

    private ProviderMeta findProviderMeta(List<ProviderMeta> providerMetas, String methodSign) {
        // 寻找方法签名是否存在
        Optional<ProviderMeta> optional = providerMetas.stream()
                .filter(x -> x.getMethodSign().equals(methodSign)).findFirst();
        return optional.orElse(null);
    }

}
