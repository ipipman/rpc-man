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
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    // 记录注册中心,服务的版本号
    Map<String, Long> VERSIONS = new HashMap<>();
    // 记录需要通过renew保活的实例与服务
    MultiValueMap<InstanceMeta, ServiceMeta> RENEWS = new LinkedMultiValueMap<>();
    // 定期检查注册中心服务+实例的版本, 用于订阅
    IpManRegistryExecutor versionChecker;
    // 定期上报服务+实例的健康状态, 用于保活
    IpManRegistryExecutor heathChecker;

    @Override
    public void start() {
        log.info(" ====>>>> [IpMan-Registry] : start with server: {}", server);
        // 定期对比与注册中心版本号, 如果版本有变化则触发回调, 用于更新 providers 的 instances, 5s一次
        versionChecker = new IpManRegistryExecutor(1_000, 5_000, TimeUnit.MILLISECONDS);
        // 定期将服务实例上报给注册中心, 避免被注册中心认为服务已死, 5s一次
        heathChecker = new IpManRegistryExecutor(5, 5, TimeUnit.SECONDS);
        heathChecker.executor(() -> RENEWS.keySet().forEach(
                // 根据所有实例, 找到对应服务, 触发renew进行服务健康状态上报, 做探活
                instance -> {
                    try {
                        StringBuilder sb = new StringBuilder();
                        for (ServiceMeta service : RENEWS.get(instance)) {
                            sb.append(service.toPath()).append(",");
                        }
                        String services = sb.toString();
                        if (services.endsWith(","))
                            services = services.substring(0, services.length() - 1);

                        Long timestamp = HttpInvoker.httpPost(
                                JSON.toJSONString(instance), reNewsPath(services), Long.class);
                        log.info(" ====>>>> [IpMan-Registry] : renew instance {} for {} at {}", instance, services, timestamp);
                    } catch (Exception e) {
                        log.error(" ====>>>> [IpMan-Registry] call registry leader error");
                    }
                }
        ));
    }

    @Override
    public void stop() {
        log.info(" ====>>>> [IpMan-Registry] : stop with server: {}", server);
        versionChecker.gracefulShutdown();
        heathChecker.gracefulShutdown();
    }

    @Override
    public void register(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>>> [IpMan-Registry] : register instance {} to {}", instance.toHttpUrl(), service.toPath());
        InstanceMeta inst = HttpInvoker.httpPost(JSON.toJSONString(instance), regPath(service), InstanceMeta.class);
        log.info(" ====>>>> [IpMan-Registry] : registered {} success", inst);
        RENEWS.add(instance, service);
    }

    @Override
    public void unregister(ServiceMeta service, InstanceMeta instance) {
        log.info(" ====>>>> [IpMan-Registry] : unregister instance {} to {}", instance.toHttpUrl(), service.toPath());
        InstanceMeta inst = HttpInvoker.httpPost(JSON.toJSONString(instance), unRegPath(service), InstanceMeta.class);
        log.info(" ====>>>> [IpMan-Registry] : unregistered {} success", inst);
        RENEWS.remove(instance, service);
    }

    @Override
    public List<InstanceMeta> fetchAll(ServiceMeta service) {
        log.info(" ====>>>> [IpMan-Registry] : find all instances for {}", service.toPath());
        List<InstanceMeta> instances = HttpInvoker.httpGet(findAllPath(service), new TypeReference<List<InstanceMeta>>() {
        });
        log.info(" ====>>>> [IpMan-Registry] : findAll = {}", instances);
        return instances;
    }


    @Override
    public void subscribe(ServiceMeta service, ChangedListener listener) {
        // 每隔5s, 去注册中心获取最新版本号,如果版本号大于当前版本, 就从注册中心同步最新实例的信息
        versionChecker.executor(() -> {
            try {
                // 获取注册中心, 最新的版本号
                Long newVersion = HttpInvoker.httpGet(versionPath(service), Long.class);
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
            } catch (Exception e) {
                log.error(" ====>>>> [IpMan-Registry] call registry leader error");
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

    private String reNewsPath(String services) {
        return server + "/renews?services=" + services;
    }
}
