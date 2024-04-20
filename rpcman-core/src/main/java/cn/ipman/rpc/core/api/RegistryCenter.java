package cn.ipman.rpc.core.api;

import cn.ipman.rpc.core.meta.InstanceMeta;
import cn.ipman.rpc.core.meta.ServiceMeta;
import cn.ipman.rpc.core.registry.ChangedListener;

import java.util.List;

/**
 * 注册中心接口，提供服务注册与发现的功能。
 *
 * @Author IpMan
 * @Date 2024/3/16 20:39
 */
public interface RegistryCenter {

    /**
     * 启动注册中心，初始化相关资源。
     */
    void start(); // provider || consumer

    /**
     * 停止注册中心，释放相关资源。
     */
    void stop(); // provider || consumer

    /**
     * 服务提供者注册服务实例。
     *
     * @param service 服务元数据
     * @param instance 服务实例元数据
     */
    void register(ServiceMeta service, InstanceMeta instance); // provider

    /**
     * 服务提供者注销服务实例。
     *
     * @param service 服务元数据
     * @param instance 服务实例元数据
     */
    void unregister(ServiceMeta service, InstanceMeta instance); // provider

    /**
     * 服务消费者获取所有服务实例。
     *
     * @param service 服务元数据
     * @return 返回该服务的所有实例元数据列表
     */
    List<InstanceMeta> fetchAll(ServiceMeta service); // consumer

    /**
     * 服务消费者订阅服务变化。
     *
     * @param service 服务元数据
     * @param listener 变化监听器
     */
    void subscribe(ServiceMeta service, ChangedListener listener); // consumer

    // heartbeat()

    /**
     * 取消服务消费者的订阅。
     */
    void unsubscribe();

    /**
     * 静态注册中心类，用于简单场景或测试。
     */
    @SuppressWarnings("unused")
    class StaticRegistryCenter implements RegistryCenter {

        List<InstanceMeta> providers;

        /**
         * 构造函数。
         *
         * @param providers 提供者实例元数据列表
         */
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
            // 在静态注册中心中不进行实际操作
        }

        @Override
        public void subscribe(ServiceMeta service, ChangedListener listener) {
            // 在静态注册中心中不进行实际操作
        }

        @Override
        public void unsubscribe() {
            // 在静态注册中心中不进行实际操作
        }

        @Override
        public void unregister(ServiceMeta ServiceMeta, InstanceMeta instance) {
            // 在静态注册中心中不进行实际操作
        }

        @Override
        public List<InstanceMeta> fetchAll(ServiceMeta service) {
            return providers;
        }
    }

}
