package cn.ipman.rpc.core.registry.ipman;

import cn.ipman.rpc.core.registry.Callback;
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
public class IpManRegistryExecutor {

    // 注册中心探活间隔, 5s
    int initialDelay;
    int delay;
    TimeUnit unit;

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");


    public IpManRegistryExecutor(int initialDelay, int delay, TimeUnit unit) {
        this.initialDelay = initialDelay;
        this.delay = delay;
        this.unit = unit;
    }

    public void gracefulShutdown() {
        executor.shutdown();
        try {
            executor.awaitTermination(1000, TimeUnit.MILLISECONDS);
            if (!executor.isTerminated()) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            // ignore
        }
    }

    public void executor(Callback callback) {
        executor.scheduleWithFixedDelay(() -> {
            log.debug(" schedule to check ipman registry ... [{}]", DTF.format(LocalDateTime.now()));
            try {
                callback.call();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, initialDelay, delay, unit);
    }

}
