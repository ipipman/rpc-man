package cn.ipman.rpc.core.meta;

import com.alibaba.fastjson.JSON;
import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 描述服务的元数据
 *
 * @Author IpMan
 * @Date 2024/3/23 14:42
 */
@Data
@Builder
public class ServiceMeta {

    private String app;
    private String namespace;
    private String env;
    private String name; //cn.ip
    private String version;

    private final Map<String, String> parameters = new HashMap<>();

    public String toPath() {
        return String.format("%s_%s_%s_%s_%s", app, namespace, env, name, version);
    }

    public String toMetas() {
        // 服务的元数据
        return JSON.toJSONString(this.getParameters());
    }
}
