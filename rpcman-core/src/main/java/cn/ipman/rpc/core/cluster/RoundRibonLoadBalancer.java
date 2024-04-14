package cn.ipman.rpc.core.cluster;

import cn.ipman.rpc.core.api.LoadBalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/16 19:53
 */
public class RoundRibonLoadBalancer<T> implements LoadBalancer<T> {

    AtomicInteger index = new AtomicInteger(0);

    @Override
    public T choose(List<T> providers) {
        if (providers == null || providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        // 轮询返回, & 0x7ffffff 保证是个正数
        return providers.get((index.getAndIncrement() & 0x7ffffff) % providers.size());
    }
}
