package cn.ipman.rpc.core.registry;


/**
 * 监控注册中心ProviderServer节点的变化
 *
 * @Author IpMan
 * @Date 2024/3/17 20:10
 */

@FunctionalInterface
public interface ChangedListener {

    void fire(Event event);

}
