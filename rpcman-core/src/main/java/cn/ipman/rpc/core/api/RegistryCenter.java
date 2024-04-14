package cn.ipman.rpc.core.api;

import cn.ipman.rpc.core.meta.InstanceMeta;
import cn.ipman.rpc.core.meta.ServiceMeta;
import cn.ipman.rpc.core.registry.ChangedListener;
import java.util.List;

/**
 * 注册中心
 *
 * @Author IpMan
 * @Date 2024/3/16 20:39
 */
public interface RegistryCenter {

    void start(); // provider || consumer

    void stop(); // provider || consumer

    // Provider侧
    void register(ServiceMeta service, InstanceMeta instance); // provider

    void unregister(ServiceMeta service, InstanceMeta instance); // provider

    // Consumer侧
    List<InstanceMeta> fetchAll(ServiceMeta service); // consumer

    void subscribe(ServiceMeta service, ChangedListener listener); // consumer

    // heartbeat()
    void unsubscribe();

    /**
     * 静态的注册中心
     */
    @SuppressWarnings("unused")
    class StaticRegistryCenter implements RegistryCenter {

        List<InstanceMeta> providers;

        public StaticRegistryCenter(List<InstanceMeta> providers) {
            this.providers = providers;
        }

        @Override
        public void start() {
            System.out.println("注册中心 start...");
        }

        @Override
        public void stop() {
            System.out.println("注册中心 stop...");
        }

        @Override
        public void register(ServiceMeta service, InstanceMeta instance) {

        }

        @Override
        public void subscribe(ServiceMeta service, ChangedListener listener) {

        }

        @Override
        public void unsubscribe() {

        }

        @Override
        public void unregister(ServiceMeta ServiceMeta, InstanceMeta instance) {

        }

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta service) {
            return providers;
        }
    }

}
