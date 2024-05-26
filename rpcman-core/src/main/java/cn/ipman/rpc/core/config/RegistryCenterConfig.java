package cn.ipman.rpc.core.config;

import cn.ipman.rpc.core.api.RegistryCenter;
import cn.ipman.rpc.core.registry.ipman.IpManRegistryCenter;
import cn.ipman.rpc.core.registry.zk.ZkRegistryCenter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/4/21 20:39
 */
@Configuration
public class RegistryCenterConfig {

    /**
     * 创建注册中心实例，默认为ZkRegistryCenter。
     * @return RegistryCenter 注册中心实例
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "rpcman.zk", value = "enabled", havingValue = "true")
    public RegistryCenter zkRc() {
        return new ZkRegistryCenter();
    }


    /**
     * 创建注册中心实例，registry-man, @linkUrl: <a href="https://github.com/ipipman/registry-man">...</a>
     * @return RegistryCenter 注册中心实例
     */
    @Bean(initMethod = "start", destroyMethod = "stop")
    @ConditionalOnProperty(prefix = "registry-ipman", value = "enabled", havingValue = "true")
    public RegistryCenter ipManRc() {
        return new IpManRegistryCenter();
    }

}
