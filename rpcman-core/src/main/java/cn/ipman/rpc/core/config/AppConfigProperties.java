package cn.ipman.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * config app properties.
 *
 * @Author IpMan
 * @Date 2024/4/4 19:27
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "rpcman.app")
public class AppConfigProperties {

    // for app instance
    private String id = "app1";

    private String namespace = "public";

    private String env = "dev";

    private String version = "1.0";

    private Boolean useNetty = false;
}
