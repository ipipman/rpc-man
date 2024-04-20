package cn.ipman.rpc.core.api;

import cn.ipman.rpc.core.config.ConsumerConfigProperties;
import cn.ipman.rpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RPC上下文环境，用于维护RPC调用过程中的上下文信息。
 *
 * @Author IpMan
 * @Date 2024/3/16 20:26
 */
@Data
public class RpcContext {

    // 过滤器链
    List<Filter> filters;

    // 路由器，用于选择合适的服务器实例
    Router<InstanceMeta> router;

    // 负载均衡器，用于平衡请求到不同的服务器实例
    LoadBalancer<InstanceMeta> loadBalancer;

    // 用户自定义参数
    private Map<String, String> parameters = new HashMap<>();

    // 消费者配置属性
    private ConsumerConfigProperties consumerProperties;

    // 线程本地存储，用于跨线程传递上下文参数，例如跟踪ID
    public static ThreadLocal<Map<String, String>> ContextParameters = ThreadLocal.withInitial(HashMap::new);

    /**
     * 获取上下文中的参数值。
     *
     * @param key 参数的键
     * @return 参数的值，如果不存在则返回null
     */
    public String param(String key) {
        return parameters.get(key);
    }

    // rpc.color = gray
    // rpc.trace_id
    // gw -> service1 ->  service2(跨线程传递) ...
    // http headers

    /**
     * 设置上下文参数。
     *
     * @param key   参数的键
     * @param value 参数的值
     */
    public static void setContextParameter(String key, String value) {
        ContextParameters.get().put(key, value);
    }

    /**
     * 获取上下文参数。
     *
     * @param key 参数的键
     * @return 参数的值，如果不存在则返回null
     */
    public static String getContextParameter(String key) {
        return ContextParameters.get().get(key);
    }

    /**
     * 移除上下文参数。
     *
     * @param key 参数的键
     */
    public static void removeContextParameter(String key) {
        ContextParameters.get().remove(key);
    }

}
