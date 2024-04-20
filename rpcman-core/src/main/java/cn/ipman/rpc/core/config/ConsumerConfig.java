package cn.ipman.rpc.core.config;

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
 * RPC消费者配置类。在RPC的Consumer端启动时，根据@RpcConsumer注解找到对应的依赖类，通过Java动态代理实现远程调用，
 * 并将代理后的Provider注入到容器中。
 *
 * @Author IpMan
 * @Date 2024/3/10 19:49
 */
@Slf4j
@Configuration
@Import({AppConfigProperties.class, ConsumerConfigProperties.class})
public class ConsumerConfig {

    @Value("${rpcman.providers:}")
    String[] services;  // 从配置中获取的RPC服务提供者列表

    @Autowired
    private AppConfigProperties appConfigProperties; // 应用配置属性

    @Autowired
    private ConsumerConfigProperties consumerConfigProperties; // 消费者配置属性

    /**
     * 创建Apollo配置变更监听器。
     * @return ApolloChangedListener 实例
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "apollo.bootstrap", value = "enabled")
    ApolloChangedListener consumer_apolloChangedListener() {
        return new ApolloChangedListener();
    }

    /**
     * 创建ConsumerBootstrap实例，用于Consumer的启动配置。
     * @return ConsumerBootstrap 实例
     */
    @Bean
    public ConsumerBootstrap createConsumerBootstrap() {
        return new ConsumerBootstrap();
    }

    /**
     * 创建ApplicationRunner，用于在Spring应用启动时执行Consumer的启动逻辑。
     * @param consumerBootstrap ConsumerBootstrap实例
     * @return ApplicationRunner 实例
     */
    @Bean
    @Order(Integer.MIN_VALUE + 1) // 让ProviderBootstrap执行顺序提前,避免Consumer依赖时找不到Provider
    public ApplicationRunner consumerBootstrapRunner(@Autowired ConsumerBootstrap consumerBootstrap) {
        return x -> {
            log.info("createConsumerBootstrap starting...");
            consumerBootstrap.start();
            log.info("createConsumerBootstrap started...");
        };
    }

    /**
     * 创建负载均衡器，默认为RoundRobinLoadBalancer。
     * @return LoadBalancer<InstanceMeta> 负载均衡器实例
     */
    @Bean
    public LoadBalancer<InstanceMeta> loadBalancer() {
        return new RoundRibonLoadBalancer<>();
    }

    /**
     * 创建路由选择器，默认为GrayRouter。
     * @return Router<InstanceMeta> 路由选择器实例
     */
    @Bean
    public Router<InstanceMeta> loadRouter() {
        return new GrayRouter(consumerConfigProperties.getGrayRatio());
    }

    /**
     * 创建注册中心实例，默认为ZkRegistryCenter。
     * @return RegistryCenter 注册中心实例
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    public RegistryCenter consumerRc() {
        return new ZkRegistryCenter();
    }


    /**
     * 创建默认过滤器，用于Consumer的请求处理链。
     * @return Filter 过滤器实例
     */
    @Bean
    public Filter filterDefault() {
        return new ContextParameterFilter();
    }

    /**
     * 创建RPC上下文，配置路由、负载均衡、过滤器等核心组件。
     * @param router 路由选择器
     * @param loadBalancer 负载均衡器
     * @param filters 过滤器列表
     * @return RpcContext RPC上下文实例
     */
    @Bean
    @RefreshScope  // 支持配置动态刷新
    public RpcContext createContext(@Autowired Router<InstanceMeta> router,
                                    @Autowired LoadBalancer<InstanceMeta> loadBalancer,
                                    @Autowired List<Filter> filters) {
        RpcContext context = new RpcContext();
        context.setRouter(router);
        context.setLoadBalancer(loadBalancer);
        context.setFilters(filters);
        // 设置应用相关参数
        context.getParameters().put("app.id", appConfigProperties.getId());
        context.getParameters().put("app.namespace", appConfigProperties.getNamespace());
        context.getParameters().put("app.env", appConfigProperties.getEnv());
        context.getParameters().put("app.version", appConfigProperties.getVersion());
        context.getParameters().put("app.useNetty", String.valueOf(appConfigProperties.getUseNetty()));
        // 配置Consumer相关属性
        context.setConsumerProperties(consumerConfigProperties);
        return context;
    }

}
