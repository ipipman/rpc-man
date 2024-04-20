package cn.ipman.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 配置 RPC 消费者属性。
 *
 * @Author IpMan
 * @Date 2024/4/4 19:27
 */

@Data
@Configuration
@ConfigurationProperties(prefix = "rpcman.consumer")
public class ConsumerConfigProperties {

    // 用于高可用性和治理的重试次数。
    private int retries = 1;

    // 调用超时时间（毫秒）。
    private int timeout = 1000;

    // 故障阈值，超过该值则进入熔断状态。
    private int faultLimit = 10;

    // 熔断恢复初始延迟时间（毫秒）。
    private int halfOpenInitialDelay = 10_000;

    // 熔断恢复后的观察时间（毫秒）。
    private int halfOpenDelay = 60_000;

    // 灰度发布的比例。
    private int grayRatio = 0;

    // 是否使用 Netty 作为传输层，默认为 false
    private Boolean useNetty = false;

}
