package cn.ipman.rpc.core.registry.ipman;

import cn.ipman.rpc.core.api.RegistryCenter;
import cn.ipman.rpc.core.meta.InstanceMeta;
import cn.ipman.rpc.core.meta.ServiceMeta;
import cn.ipman.rpc.core.registry.ChangedListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/4/21 13:17
 */
@Slf4j
public class IpManRegistryCenter implements RegistryCenter {

    @Value("${registry-ipman.servers}")
    String server;

    private final Map<String, Long> VERSIONS = new HashMap<>();

    @Override
    public void start() {
        log.info(" ====>>>> [IpMan-Registry] : start with server: {}", server);
    }

    @Override
    public void stop() {
        log.info(" ====>>>> [IpMan-Registry] : stop with server: {}", server);
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>>> [IpMan-Registry] : register instance {} to {}", instance.toHttpUrl(), service.toPath());
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {

    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        return null;
    }

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {

    }

    @Override
    public void unsubscribe() {

    }
}
