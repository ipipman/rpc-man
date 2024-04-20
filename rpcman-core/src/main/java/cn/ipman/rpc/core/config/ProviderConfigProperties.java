package cn.ipman.rpc.core.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * RPC提供者配置属性类。用于配置RPC服务提供者的相关属性。
 *
 * @Author IpMan
 * @Date 2024/4/4 19:27
 */
@Data
@Configuration // 表示这是一个Spring配置类
@ConfigurationProperties(prefix = "rpcman.provider")
public class ProviderConfigProperties {

    // 用于存储RPC提供者的元数据信息，键值对形式。
    Map<String, String> metas = new HashMap<>();

}
