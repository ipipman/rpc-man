package cn.ipman.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 配置应用程序属性。
 * 该类用于定义RPC管理器应用的配置属性，包括应用实例ID、命名空间、环境、版本号及是否使用Netty等设置。
 *
 * @Author IpMan
 * @Date 2024/4/4 19:27
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rpcman.app")
public class AppConfigProperties {

    // 应用实例ID，默认为"app1"。
    private String id = "app1";

    // 命名空间，默认为"public"。
    private String namespace = "public";

    // 环境，默认为"dev"。
    private String env = "dev";

    // 版本号，默认为"1.0"。
    private String version = "1.0";

    // 是否使用Netty作为传输协议，默认为false
    private Boolean useNetty = false;
}
