package cn.ipman.rpc.core.provider;

import cn.ipman.rpc.core.api.RegistryCenter;
import cn.ipman.rpc.core.config.AppConfigProperties;
import cn.ipman.rpc.core.config.ProviderConfigProperties;
import cn.ipman.rpc.core.meta.InstanceMeta;
import cn.ipman.rpc.core.meta.ProviderMeta;
import cn.ipman.rpc.core.meta.ServiceMeta;
import cn.ipman.rpc.core.util.MethodUtils;
import cn.ipman.rpc.core.annotation.RpcProvider;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.Map;


/**
 * Description for this class
 * RPC生产者启动程序,负责Provider类初始化及调用
 *
 * @Author IpMan
 * @Date 2024/3/9 20:07
 */
@Data
@Slf4j
public class ProviderBootstrap implements ApplicationContextAware {

    // 容器上下文
    private ApplicationContext applicationContext;
    // 注册中心
    private RegistryCenter rc;
    // 方法名 -> [sign1, sign2]
    private MultiValueMap<String, ProviderMeta> skeleton = new LinkedMultiValueMap<>();
    // Provider服务实例信息
    private String port;
    private InstanceMeta instance;

    private AppConfigProperties appProperties;
    private ProviderConfigProperties providerProperties;

    public ProviderBootstrap(String port, AppConfigProperties appConfigProperties,
                             ProviderConfigProperties providerConfigProperties) {
        this.port = port;
        this.appProperties = appConfigProperties;
        this.providerProperties = providerConfigProperties;
    }

    @PostConstruct
    @SneakyThrows
    public void init() {
        // 寻找@Provider的实现类
        Map<String, Object> providers = applicationContext.getBeansWithAnnotation(RpcProvider.class);
        // 获取注册中心
        this.rc = applicationContext.getBean(RegistryCenter.class);
        providers.forEach((className, classObject)
                -> log.info("@RpcProvider init, className=" + className + ",classObject=" + classObject));
        // 初始化接口列表
        providers.values().forEach(this::genInterface);

    }

    @SneakyThrows
    public void start() {
        // 获取provider实例, 注册到 zookeeper
        String ip = InetAddress.getLocalHost().getHostAddress();
        // metas =  // 添加机房、灰度、单元配置
        if (appProperties.getUseNetty()) {
            this.instance = InstanceMeta.http(ip, Integer.parseInt(port) + 1000)
                    .addParams(providerProperties.getMetas());
        } else {
            this.instance = InstanceMeta.http(ip, Integer.parseInt(port)).addParams(providerProperties.getMetas());
        }
        // 启动注册中心连接,开始注册
        // this.rc.start();
        this.skeleton.keySet().forEach(this::registerService);

    }

    @PreDestroy
    public void stop() {
        log.info(" ===> zk PreDestroy stop: " + this.skeleton);
        // 取消注册,关闭注册中心连接
        skeleton.keySet().forEach(this::unregisterService);
        // rc.stop();
    }

    private void unregisterService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .name(service)
                .app(appProperties.getId())
                .namespace(appProperties.getNamespace())
                .env(appProperties.getEnv())
                .version(appProperties.getVersion())
                .build();
        rc.unregister(serviceMeta, this.instance);
    }

    private void registerService(String service) {
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .name(service)
                .app(appProperties.getId())
                .namespace(appProperties.getNamespace())
                .env(appProperties.getEnv())
                .version(appProperties.getVersion())
                .build();
        rc.register(serviceMeta, this.instance);
    }

    private void genInterface(Object impl) {
        // 获取注入类的实例,并注册到  skeleton <className, classObject>
        Class<?>[] services = impl.getClass().getInterfaces();
        for (Class<?> service : services) {
            Method[] methods = service.getMethods();
            for (Method method : methods) {
                // 如果是本地方法,就跳过
                if (MethodUtils.checkLocalMethod(method)) {
                    continue;
                }
                // 创建 skeleton
                createProvider(service, impl, method);
            }
        }
    }

    private void createProvider(Class<?> service, Object impl, Method method) {
        ProviderMeta providerMeta = ProviderMeta.builder()
                .method(method).methodSign(MethodUtils.methodSign(method)).serviceImpl(impl)
                .build();
        log.info("create a provider:" + providerMeta);
        this.skeleton.add(service.getCanonicalName(), providerMeta);
    }

    @Deprecated
    @SuppressWarnings("unused")
    private Method findMethod(Class<?> aClass, String methodName) {
        // 根据实现类的方法名字,查找方法实例
        // TODO: 如果有方法重载,这种实现并不可靠,待完善
        for (Method method : aClass.getMethods()) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        return null;
    }

}
