package cn.ipman.rpc.core.api;

import java.util.List;

/**
 * 负载均衡, weightedRR, autoRR-自适应
 * 8081, w= 100, 25次
 * 8082, w= 300, 75次
 * <p>
 * 0-99, random, <25, -8081, else 8082
 * <p>
 * UseService 10...
 * 8081, 10ms
 * 8082, 100ms
 * <p>
 * avg * 0.3  + last * 0.7 = w* ~
 */
public interface LoadBalancer<T> {

    /**
     * 选择provider
     */
    T choose(List<T> providers);

    @SuppressWarnings("unused")
    LoadBalancer<?> Default = p -> (p == null || p.isEmpty()) ? null : p.get(0);


}
