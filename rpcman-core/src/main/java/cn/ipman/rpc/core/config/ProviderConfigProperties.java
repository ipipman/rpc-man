package cn.ipman.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * config provider properties.
 *
 * @Author IpMan
 * @Date 2024/4/4 19:27
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "rpcman.provider")
public class ProviderConfigProperties {

    // for provider
    Map<String, String> metas = new HashMap<>();

}
