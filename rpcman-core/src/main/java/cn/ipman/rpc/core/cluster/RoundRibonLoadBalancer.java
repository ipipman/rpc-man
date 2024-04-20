package cn.ipman.rpc.core.cluster;

import cn.ipman.rpc.core.api.LoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 圆环负载均衡器，实现负载均衡接口。
 * 以循环的方式选择服务提供者，实现请求的均衡分发。
 *
 * @Author IpMan
 * @Date 2024/3/16 19:53
 */
public class RoundRibonLoadBalancer<T> implements LoadBalancer<T> {

    // 使用原子整型 AtomicInteger 来保证线程安全的索引操作
    AtomicInteger index = new AtomicInteger(0);

    /**
     * 从服务提供者列表中选择一个服务提供者。
     *
     * @param providers 服务提供者列表，类型为泛型 T 的 List。
     * @return 返回选择的服务提供者，如果列表为空或只有一个提供者，则直接返回；否则按轮询方式选择并返回。
     */
    @Override
    public T choose(List<T> providers) {
        // 判断服务提供者列表是否为空或者为空列表
        if (providers == null || providers.isEmpty()) return null;
        // 如果只有一个服务提供者，直接返回该提供者
        if (providers.size() == 1) return providers.get(0);
        // 轮询选择服务提供者，使用取模运算确保索引在列表大小范围内，& 0x7ffffff 保证结果为正数
        return providers.get((index.getAndIncrement() & 0x7ffffff) % providers.size());
    }
}
