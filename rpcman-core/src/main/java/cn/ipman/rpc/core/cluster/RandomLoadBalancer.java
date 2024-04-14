package cn.ipman.rpc.core.cluster;

import cn.ipman.rpc.core.api.LoadBalancer;

import java.util.List;
import java.util.Random;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/16 19:53
 */
@SuppressWarnings("unused")
public class RandomLoadBalancer<T> implements LoadBalancer<T> {

    Random random = new Random();

    @Override
    public T choose(List<T> providers) {
        if (providers == null || providers.isEmpty()) return null;
        if (providers.size() == 1) return providers.get(0);
        // 随机返回
        return providers.get(random.nextInt(providers.size()));
    }
}
