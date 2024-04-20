package cn.ipman.rpc.core.config;

import com.ctrip.framework.apollo.model.ConfigChange;
import com.ctrip.framework.apollo.model.ConfigChangeEvent;
import com.ctrip.framework.apollo.spring.annotation.ApolloConfigChangeListener;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * 用于监听Apollo配置变更事件，并更新对应Bean的属性值。
 * 主要作用于使用了@ConfigurationProperties注解的Bean。
 *
 * @Author IpMan
 * @Date 2024/4/13 16:37
 */
@Data
@Slf4j
public class ApolloChangedListener implements ApplicationContextAware {

    ApplicationContext applicationContext;

    /**
     * 当Apollo配置发生变更时触发的处理方法。
     * 会遍历所有变更的配置项，并记录日志。
     * 然后发布一个环境变更事件，以触发属性值的更新。
     *
     * @param changeEvent 包含配置变更详细信息的事件对象。
     */
    @ApolloConfigChangeListener({"rpcman-app"}) // listener to namespace
    @SuppressWarnings("unused")
    private void changeHandler(ConfigChangeEvent changeEvent) {
        // 遍历所有变更的配置键
        for (String key : changeEvent.changedKeys()) {
            ConfigChange change = changeEvent.getChange(key);
            log.info("Found change - {}", change.toString());
        }

        // 发布环境变更事件，以触发使用@ConfigurationProperties注解的Bean的属性更新
        this.applicationContext.publishEvent(new EnvironmentChangeEvent(changeEvent.changedKeys()));
    }
}
