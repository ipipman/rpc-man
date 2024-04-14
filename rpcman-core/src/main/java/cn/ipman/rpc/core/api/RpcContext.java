package cn.ipman.rpc.core.api;

import cn.ipman.rpc.core.config.ConsumerConfigProperties;
import cn.ipman.rpc.core.meta.InstanceMeta;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/16 20:26
 */
@Data
public class RpcContext {

    List<Filter> filters;

    Router<InstanceMeta> router;

    LoadBalancer<InstanceMeta> loadBalancer;

    private Map<String, String> parameters = new HashMap<>();

    private ConsumerConfigProperties consumerProperties;

    public static ThreadLocal<Map<String, String>> ContextParameters = ThreadLocal.withInitial(HashMap::new);

    public String param(String key) {
        return parameters.get(key);
    }

    // rpc.color = gray
    // rpc.trace_id
    // gw -> service1 ->  service2(跨线程传递) ...
    // http headers

    public static void setContextParameter(String key, String value) {
        ContextParameters.get().put(key, value);
    }

    public static String getContextParameter(String key) {
        return ContextParameters.get().get(key);
    }

    public static void removeContextParameter(String key) {
        ContextParameters.get().remove(key);
    }

}
