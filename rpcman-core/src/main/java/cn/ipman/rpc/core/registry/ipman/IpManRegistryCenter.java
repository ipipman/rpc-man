package cn.ipman.rpc.core.registry.ipman;

import cn.ipman.rpc.core.api.RegistryCenter;
import cn.ipman.rpc.core.consumer.HttpInvoker;
import cn.ipman.rpc.core.meta.InstanceMeta;
import cn.ipman.rpc.core.meta.ServiceMeta;
import cn.ipman.rpc.core.registry.ChangedListener;
import cn.ipman.rpc.core.registry.Event;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
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
        InstanceMeta inst = HttpInvoker.httpPost(JSON.toJSONString(instance), regPath(service), InstanceMeta.class);
        log.info(" ====>>>> [IpMan-Registry] : registered {} success", inst);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>>> [IpMan-Registry] : unregister instance {} to {}", instance.toHttpUrl(), service.toPath());
        InstanceMeta inst = HttpInvoker.httpPost(JSON.toJSONString(instance), unRegPath(service), InstanceMeta.class);
        log.info(" ====>>>> [IpMan-Registry] : unregistered {} success", inst);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ====>>>> [IpMan-Registry] : find all instances for {}", service.toPath());
        List<InstanceMeta> instances = HttpInvoker.httpGet(findAllPath(service), new TypeReference<List<InstanceMeta>>() {
        });
        log.info(" ====>>>> [IpMan-Registry] : findAll = {}", instances);
        return instances;
    }

    IpManHeathChecker heathChecker = new IpManHeathChecker();

    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        // 每隔5s, 去注册中心获取最新版本号,如果版本号大于当前版本, 就从注册中心同步最新实例的信息
        heathChecker.check(() -> {
            // 获取注册中心, 最新的版本号
            String versionPath = versionPath(service);
            Long newVersion = HttpInvoker.httpGet(versionPath, Long.class);
            Long version = VERSIONS.getOrDefault(service.toPath(), -1L);
            log.debug(" ====>>>> [{}] newVersion:{} oldVersion:{}", service.toPath(), newVersion, version);

            // 如果版本号大于当前版本, 就从注册中心同步最新实例的信息
            if (newVersion > version) {
                log.info(" ====>>>> version changed [{}] newVersion:{} oldVersion:{}", service.toPath(), newVersion, version);
                List<InstanceMeta> instanceMetas = fetchAll(service);
                log.info(" ====>>>> version {} fetch all and fire: {}", newVersion, instanceMetas);
                listener.fire(new Event(instanceMetas));
                VERSIONS.put(service.toPath(), newVersion);
            }
        });
    }

    @Override
    public void unsubscribe() {

    }

    private String regPath(ServiceMeta service) {
        return server + "/reg?service=" + service.toPath();
    }

    private String unRegPath(ServiceMeta service) {
        return server + "/unreg?service=" + service.toPath();
    }

    private String findAllPath(ServiceMeta service) {
        return server + "/findall?service=" + service.toPath();
    }

    private String versionPath(ServiceMeta service) {
        return server + "/version?service=" + service.toPath();
    }
}
