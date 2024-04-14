package cn.ipman.rpc.core.consumer;

import cn.ipman.rpc.core.api.RegistryCenter;
import cn.ipman.rpc.core.api.RpcContext;
import cn.ipman.rpc.core.meta.InstanceMeta;
import cn.ipman.rpc.core.meta.ServiceMeta;
import cn.ipman.rpc.core.util.MethodUtils;
import cn.ipman.rpc.core.annotation.RpcConsumer;
import cn.ipman.rpc.core.api.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/3/10 19:47
 */
@Data
@Slf4j
public class ConsumerBootstrap implements ApplicationContextAware, EnvironmentAware {

    ApplicationContext applicationContext;
    Environment environment;

    private Map<String, Object> stub = new HashMap<>();

    public void start() {

        RegistryCenter rc = applicationContext.getBean(RegistryCenter.class);
        RpcContext rpcContext = applicationContext.getBean(RpcContext.class);

        // 获取Spring容器中所有的Bean
        String[] names = applicationContext.getBeanDefinitionNames();
        for (String name : names) {
            // 根据Bean的名称,获取实例如: rpcmanDemoConsumerApplication
            Object bean = applicationContext.getBean(name);
            // debug -> if (!name.contains("rpcmanDemoConsumerApplication")) continue;

            // 通过Java反射获取标记 @RpcConsumer 注解的类成员,
            // 如:cn.ipman.rpc.demo.consumer.RpcmanDemoConsumerApplication.userService
            List<Field> fields = MethodUtils.findAnnotatedFiled(bean.getClass(), RpcConsumer.class);
            fields.forEach(f -> {
                // 获取成员类实例
                Class<?> service = f.getType();
                // 获取成员类实例的类名,如:cn.ipman.rpc.demo.api.UserService
                String serviceName = service.getCanonicalName();
                log.info(" ===> " + f.getName());
                try {
                    Object consumer = stub.get(serviceName);
                    if (consumer == null) {
                        // 给成员类实例添加Java动态代理
                        consumer = createFromRegistry(service, rpcContext, rc);
                        stub.put(serviceName, consumer);
                    }
                    // 设置可操作权限
                    f.setAccessible(true);
                    // 将动态代理后的Provider类, 重新注入如到Spring容器中
                    // 这样调用Provider时, 通过动态代理实现远程调用
                    f.set(bean, consumer);
                } catch (Exception ex) {
                    log.warn(" ==> Field[{}.{}] create consumer failed.", serviceName, f.getName());
                    log.error("Ignore and print it as: ", ex);
                }
            });
        }
    }


    private Object createFromRegistry(Class<?> service, RpcContext rpcContext, RegistryCenter rc) {
        String serviceName = service.getCanonicalName();
        ServiceMeta serviceMeta = ServiceMeta.builder()
                .name(serviceName)
                .app(rpcContext.param("app.id"))
                .namespace(rpcContext.param("app.namespace"))
                .env(rpcContext.param("app.env"))
                .version(rpcContext.param("app.version"))
                .build();
        List<InstanceMeta> providers = rc.fetchAll(serviceMeta);
        log.debug("  ===> map to providers");
        providers.forEach(x -> log.debug("InstanceMeta providers={}", x));

        // 新增Provider节点订阅
        rc.subscribe(serviceMeta, event -> {
            providers.clear();
            providers.addAll(event.getData());
        });
        return createConsumer(service, rpcContext, providers);
    }


    private Object createConsumer(Class<?> service, RpcContext rpcContext, List<InstanceMeta> providers) {
        // 通过Java动态代理,实现 Provider 的远程调用
        return Proxy.newProxyInstance(service.getClassLoader(),
                new Class[]{service}, new RpcInvocationHandler(service, rpcContext, providers));
    }


}
