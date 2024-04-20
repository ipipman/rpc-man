package cn.ipman.rpc.core.config;

import cn.ipman.rpc.core.api.RegistryCenter;
import cn.ipman.rpc.core.provider.ProviderBootstrap;
import cn.ipman.rpc.core.provider.ProviderInvoker;
import cn.ipman.rpc.core.provider.http.NettyServer;
import cn.ipman.rpc.core.registry.zk.ZkRegistryCenter;
import cn.ipman.rpc.core.transport.SpringBootTransport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;


/**
 * 提供者配置类，负责配置并初始化RPC提供者相关组件，将其注册到Spring容器中。
 *
 * @Author IpMan
 * @Date 2024/3/9 20:07
 */
@Slf4j
@Configuration
@Import({AppConfigProperties.class, ProviderConfigProperties.class, SpringBootTransport.class})
public class ProviderConfig {

    @Value("${server.port:8081}")
    private String port;

    /**
     * 创建Apollo配置变更监听器Bean，仅当配置开启时生效。
     *
     * @return ApolloChangedListener 配置变更监听器实例。
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener provider_apolloChangedListener() {
        return new ApolloChangedListener();
    }

    /**
     * 创建Provider启动器Bean。
     *
     * @param appConfigProperties 应用配置属性。
     * @param providerConfigProperties 提供者配置属性。
     * @return ProviderBootstrap 提供者启动器实例。
     */
    @Bean
    ProviderBootstrap providerBootstrap(@Autowired AppConfigProperties appConfigProperties,
                                        @Autowired ProviderConfigProperties providerConfigProperties) {
        return new ProviderBootstrap(port, appConfigProperties, providerConfigProperties);
    }

    /**
     * 创建Provider调用器Bean。
     *
     * @param providerBootstrap 提供者启动器实例。
     * @return ProviderInvoker 提供者调用器实例。
     */
    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    /**
     * 创建应用启动运行器Bean，用于在应用启动时运行提供者启动器。
     *
     * @param providerBootstrap 提供者启动器实例。
     * @return ApplicationRunner 应用启动运行器实例。
     */
    @Bean
    @Order(Integer.MIN_VALUE) // 让ProviderBootstrap执行顺序提前,避免Consumer依赖时找不到Provider
    public ApplicationRunner providerBootstrapRunner(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> {
            log.info("createProviderBootstrap starting...");
            providerBootstrap.start();
            log.info("createProviderBootstrap started...");
        };
    }

    /**
     * 创建注册中心Bean，如果容器中不存在则创建一个新的ZkRegistryCenter实例。
     *
     * @return RegistryCenter 注册中心实例。
     */
    @Bean
    @ConditionalOnMissingBean
    public RegistryCenter consumer_rc() {
        return new ZkRegistryCenter();
    }

    /**
     * 创建Netty服务器Bean，用于提供HTTP服务。
     *
     * @param appConfigProperties 应用配置属性。
     * @param providerInvoker 提供者调用器实例。
     * @return NettyServer Netty服务器实例。
     */
    @Bean(initMethod = "start")
    public NettyServer nettyServer(@Autowired AppConfigProperties appConfigProperties,
                                   @Autowired ProviderInvoker providerInvoker) {
        if (appConfigProperties.getUseNetty())
            return new NettyServer(Integer.parseInt(port) + 1000, providerInvoker);
        return null;
    }

}