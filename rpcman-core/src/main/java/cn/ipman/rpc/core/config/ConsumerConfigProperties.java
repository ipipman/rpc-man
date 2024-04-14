package cn.ipman.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * config consumer properties.
 *
 * @Author IpMan
 * @Date 2024/4/4 19:27
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "rpcman.consumer")
public class ConsumerConfigProperties {

    // for ha and governance
    private int retries = 1;

    private int timeout = 1000;

    private int faultLimit = 10;

    private int halfOpenInitialDelay = 10_000;

    private int halfOpenDelay = 60_000;

    private int grayRatio = 0;

    private Boolean useNetty = false;

}
