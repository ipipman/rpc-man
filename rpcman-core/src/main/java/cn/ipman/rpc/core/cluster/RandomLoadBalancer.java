package cn.ipman.rpc.core.cluster;

import cn.ipman.rpc.core.api.LoadBalancer;

import java.util.List;
import java.util.Random;

/**
 * 随机负载均衡器。从提供者列表中随机选择一个服务提供者。
 *
 * @Author IpMan
 * @Date 2024/3/16 19:53
 */
@SuppressWarnings("unused")
public class RandomLoadBalancer<T> implements LoadBalancer<T> {

    Random random = new Random(); // 使用随机数生成器

    /**
     * 从给定的服务提供者列表中随机选择一个服务提供者。
     *
     * @param providers 服务提供者列表。不可为null且至少应包含一个元素。
     * @return 随机选择的服务提供者。如果列表为空或null，则返回null；如果列表只有一个元素，则返回该元素。
     */
    @Override
    public T choose(List<T> providers) {
        // 判断列表是否为空或null
        if (providers == null || providers.isEmpty()) return null;
        // 如果只有一个元素，直接返回
        if (providers.size() == 1) return providers.get(0);
        // 随机选择一个服务提供者并返回
        return providers.get(random.nextInt(providers.size()));
    }
}
