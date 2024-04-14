package cn.ipman.rpc.core.config;

import cn.ipman.rpc.core.api.*;
import cn.ipman.rpc.core.api.*;
import cn.ipman.rpc.core.cluster.GrayRouter;
import cn.ipman.rpc.core.cluster.RoundRibonLoadBalancer;
import cn.ipman.rpc.core.consumer.ConsumerBootstrap;
import cn.ipman.rpc.core.filter.ContextParameterFilter;
import cn.ipman.rpc.core.meta.InstanceMeta;
import cn.ipman.rpc.core.registry.zk.ZkRegistryCenter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.Order;

import java.util.List;


/**
 * Description for this class
 * RPC的Consumer端启动时,根据@RpcConsumer注解找到对应的依赖类,通过Java动态代理实现远程调用,并将代理后的Provider注入到容器种
 *
 * @Author IpMan
 * @Date 2024/3/10 19:49
 */
@Slf4j
@Configuration
@Import({AppConfigProperties.class, ConsumerConfigProperties.class})
public class ConsumerConfig {

    @Value("${rpcman.providers:}")
    String[] services;

    @Autowired
    private AppConfigProperties appConfigProperties;

    @Autowired
    private ConsumerConfigProperties consumerConfigProperties;

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener consumer_apolloChangedListener() {
        return new ApolloChangedListener();
    }

    @Bean
    public ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    @Bean
    @Order(Integer.MIN_VALUE + 1) // 让ProviderBootstrap执行顺序提前,避免Consumer依赖时找不到Provider
    public ApplicationRunner consumerBootstrapRunner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            log.info("createConsumerBootstrap starting...");
            consumerBootstrap.start();
            log.info("createConsumerBootstrap started...");
        };
    }

    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        return new RoundRibonLoadBalancer<>();
    }

    @Bean
    public Router<InstanceMeta> loadRouter() {
        return new GrayRouter(consumerConfigProperties.getGrayRatio());
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter consumerRc() {
        return new ZkRegistryCenter();
    }

    @Bean
    public Filter filterDefault() {
        return new ContextParameterFilter();
    }

    @Bean
    @RefreshScope // context.refresh
    public RpcContext createContext(@Autowired Router<InstanceMeta> router,
                                    @Autowired LoadBalancer<InstanceMeta> loadBalancer,
                                    @Autowired List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        context.getParameters().put("app.id", appConfigProperties.getId());
        context.getParameters().put("app.namespace", appConfigProperties.getNamespace());
        context.getParameters().put("app.env", appConfigProperties.getEnv());
        context.getParameters().put("app.version", appConfigProperties.getVersion());
        context.getParameters().put("app.useNetty", String.valueOf(appConfigProperties.getUseNetty()));
        // 重试、超时等配置
        context.setConsumerProperties(consumerConfigProperties);
        return context;
    }

}
