package cn.ipman.rpc.core.api;

import java.util.List;

/**
 * 负载均衡器接口，支持加权轮询（weightedRR）和自适应轮询（autoRR）算法。
 * 例如，假设有两个服务提供者：
 * - 8081 端口，权重为 100，在一轮中应该被选择 25 次；
 * - 8082 端口，权重为 300，在一轮中应该被选择 75 次。
 * <p>
 * 请求被分配到服务提供者的策略为：
 * - 随机选择一个数字 0-99；
 * - 如果数字小于 25，则选择 8081 端口，否则选择 8082 端口。
 * <p>
 * 在使用服务时，考虑响应时间进行动态权重调整的示例：
 * - 第一次请求，8081 端口用时 10ms，8082 端口用时 100ms。
 * - 下一次选择时，根据平均响应时间（avg）的 30% 和上一次响应时间（last）的 70% 来动态计算权重。
 */
public interface LoadBalancer<T> {

    /**
     * 从服务提供者列表中选择一个服务提供者。
     *
     * @param providers 服务提供者列表。
     * @return 返回选择的服务提供者，如果没有可用的服务提供者，则返回 null。
     */
    T choose(List<T> providers);

    // 默认负载均衡策略，选择列表中的第一个服务提供者
    @SuppressWarnings("unused")
    LoadBalancer<?> Default = p -> (p == null || p.isEmpty()) ? null : p.get(0);


}
