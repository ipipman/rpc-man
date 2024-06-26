package cn.ipman.rpc.core.annotation;

import cn.ipman.rpc.core.config.ConsumerConfig;
import cn.ipman.rpc.core.config.ProviderConfig;
import cn.ipman.rpc.core.config.RegistryCenterConfig;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Inherited
@Import({ProviderConfig.class, ConsumerConfig.class, RegistryCenterConfig.class})
@SuppressWarnings("unused")
public @interface EnableRpcMan {
}
