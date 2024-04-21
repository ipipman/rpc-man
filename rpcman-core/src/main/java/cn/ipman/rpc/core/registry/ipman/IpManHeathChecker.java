package cn.ipman.rpc.core.registry.ipman;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Description for this class
 *
 * @Author IpMan
 * @Date 2024/4/21 20:12
 */
@Slf4j
public class IpManHeathChecker {

    // 注册中心探活间隔, 5s
    final int interval = 5_000;

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");

    public void check(Callback callback) {
        executor.scheduleWithFixedDelay(() -> {
            log.debug(" schedule to check ipman registry ... [{}]", DTF.format(LocalDateTime.now()));
            try {
                callback.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, interval, interval, TimeUnit.MILLISECONDS);
    }


    @FunctionalInterface
    public interface Callback {
        void call() throws Exception;
    }
}
