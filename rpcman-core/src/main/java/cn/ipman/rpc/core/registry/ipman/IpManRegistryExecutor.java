package cn.ipman.rpc.core.registry.ipman;

import cn.ipman.rpc.core.registry.Callback;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * IpMan注册中心执行器，负责周期性执行注册中心相关的任务。
 *
 * @Author IpMan
 * @Date 2024/4/21 20:12
 */
@Slf4j
public class IpManRegistryExecutor {

    // 注册中心探活间隔, 5s
    int initialDelay;
    // 周期性执行的间隔时间
    int delay;
    // 时间单位
    TimeUnit unit;

    // 使用单线程定时任务执行器，用于周期性执行注册中心的任务。
    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    // 日期格式化器，用于格式化日志输出中的时间。
    static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("uuuu-MM-dd HH:mm:ss");


    /**
     * 构造函数，初始化注册中心执行器。
     *
     * @param initialDelay 初始延迟时间
     * @param delay 周期性执行的间隔时间
     * @param unit 时间单位
     */
    public IpManRegistryExecutor(int initialDelay, int delay, TimeUnit unit) {
        this.initialDelay = initialDelay;
        this.delay = delay;
        this.unit = unit;
    }

    /**
     * 优雅关闭执行器服务。
     * 尝试等待所有任务完成，超时后强制关闭。
     */
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

    /**
     * 提交一个注册中心回调任务，周期性执行。
     *
     * @param callback 注册中心任务的回调接口
     */
    public void executor(Callback callback) {
        // 定时并周期性执行回调任务，记录日志并执行回调函数
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
