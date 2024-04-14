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
 * Description for this class
 * 将Provider启动项,配置到Spring容器中
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

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener provider_apolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Bean
    ProviderBootstrap providerBootstrap(@Autowired AppConfigProperties appConfigProperties,
                                        @Autowired ProviderConfigProperties providerConfigProperties) {
        return new ProviderBootstrap(port, appConfigProperties, providerConfigProperties);
    }

    @Bean
    ProviderInvoker providerInvoker(@Autowired ProviderBootstrap providerBootstrap) {
        return new ProviderInvoker(providerBootstrap);
    }

    @Bean
    @Order(Integer.MIN_VALUE) // 让ProviderBootstrap执行顺序提前,避免Consumer依赖时找不到Provider
    public ApplicationRunner providerBootstrapRunner(@Autowired ProviderBootstrap providerBootstrap) {
        return x -> {
            log.info("createProviderBootstrap starting...");
            providerBootstrap.start();
            log.info("createProviderBootstrap started...");
        };
    }

    @Bean
    @ConditionalOnMissingBean
    public RegistryCenter consumer_rc() {
        return new ZkRegistryCenter();
    }

    @Bean(initMethod = "start")
    public NettyServer nettyServer(@Autowired AppConfigProperties appConfigProperties,
                                   @Autowired ProviderInvoker providerInvoker) {
        if (appConfigProperties.getUseNetty())
            return new NettyServer(Integer.parseInt(port) + 1000, providerInvoker);
        return null;
    }

}