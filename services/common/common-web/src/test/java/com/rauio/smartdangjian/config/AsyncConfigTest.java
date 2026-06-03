package com.rauio.smartdangjian.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@DisplayName("AsyncConfig 线程池配置测试")
class AsyncConfigTest {

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
    private final AsyncConfig asyncConfig = new AsyncConfig(meterRegistry);

    @Test
    @DisplayName("IO 线程池使用调用方执行拒绝策略并支持优雅关闭")
    void ioTaskExecutorUsesCallerRunsAndGracefulShutdown() {
        ThreadPoolTaskExecutor executor = asThreadPoolTaskExecutor(asyncConfig.ioTaskExecutor());

        assertResilientExecutor(executor);
        assertThat(executor.getThreadNamePrefix()).isEqualTo("IO-Task-");
    }

    @Test
    @DisplayName("CPU 线程池显式使用调用方执行拒绝策略并支持优雅关闭")
    void cpuTaskExecutorUsesCallerRunsAndGracefulShutdown() {
        ThreadPoolTaskExecutor executor = asThreadPoolTaskExecutor(asyncConfig.cpuTaskExecutor());

        assertResilientExecutor(executor);
        assertThat(executor.getThreadNamePrefix()).isEqualTo("CPU-Task-");
    }

    @Test
    @DisplayName("长任务线程池不再静默丢弃任务并支持优雅关闭")
    void longTaskExecutorUsesCallerRunsAndGracefulShutdown() {
        ThreadPoolTaskExecutor executor = asThreadPoolTaskExecutor(asyncConfig.longTaskExecutor());

        assertResilientExecutor(executor);
        assertThat(executor.getThreadNamePrefix()).isEqualTo("Long-Task-");
    }

    @Test
    @DisplayName("MVC 异步线程池使用调用方执行拒绝策略并支持优雅关闭")
    void mvcTaskExecutorUsesCallerRunsAndGracefulShutdown() {
        ThreadPoolTaskExecutor executor = asyncConfig.mvcTaskExecutor();

        assertResilientExecutor(executor);
        assertThat(executor.getThreadNamePrefix()).isEqualTo("mvc-async-");
    }

    @Test
    @DisplayName("每个线程池都在 SimpleMeterRegistry 中注册了 executor 指标")
    void eachExecutorRegistersMetricsInMeterRegistry() {
        asyncConfig.ioTaskExecutor();
        asyncConfig.cpuTaskExecutor();
        asyncConfig.longTaskExecutor();
        asyncConfig.mvcTaskExecutor();

        assertThat(meterRegistry.getMeters())
                .extracting(m -> m.getId().getTag("name"))
                .anyMatch("io-task-executor"::equals)
                .anyMatch("cpu-task-executor"::equals)
                .anyMatch("long-task-executor"::equals)
                .anyMatch("mvc-async-executor"::equals);
    }

    @Test
    @DisplayName("指标包含线程池关键度量：executor.completed、executor.active、executor.pool.size")
    void metricsContainThreadPoolKeyMetrics() {
        asyncConfig.ioTaskExecutor();

        assertThat(meterRegistry.getMeters())
                .extracting(m -> m.getId().getName())
                .anyMatch("executor.completed"::equals)
                .anyMatch("executor.active"::equals)
                .anyMatch("executor.pool.size"::equals);
    }

    @Test
    @DisplayName("拒绝策略保持为 CallerRunsPolicy 未被改回")
    void rejectedPolicyRemainsCallerRuns() {
        ThreadPoolTaskExecutor executor = asThreadPoolTaskExecutor(asyncConfig.ioTaskExecutor());

        assertThat(executor.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
    }

    private static ThreadPoolTaskExecutor asThreadPoolTaskExecutor(Executor executor) {
        assertThat(executor).isInstanceOf(ThreadPoolTaskExecutor.class);
        return (ThreadPoolTaskExecutor) executor;
    }

    private static void assertResilientExecutor(ThreadPoolTaskExecutor executor) {
        assertThat(executor.getThreadPoolExecutor().getRejectedExecutionHandler())
                .isInstanceOf(ThreadPoolExecutor.CallerRunsPolicy.class);
        assertThat(executor).extracting("waitForTasksToCompleteOnShutdown").isEqualTo(true);
        assertThat(executor).extracting("awaitTerminationMillis").isEqualTo(30_000L);
    }
}
