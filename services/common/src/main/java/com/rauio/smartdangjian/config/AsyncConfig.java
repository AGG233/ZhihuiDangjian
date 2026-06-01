package com.rauio.smartdangjian.config;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.AsyncSupportConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;
import lombok.RequiredArgsConstructor;

@AutoConfiguration
@EnableAsync
@EnableScheduling
@RequiredArgsConstructor
public class AsyncConfig implements WebMvcConfigurer {

    private final MeterRegistry meterRegistry;

    @Override
    public void configureAsyncSupport(AsyncSupportConfigurer configurer) {

        configurer.setTaskExecutor(mvcTaskExecutor());
        configurer.setDefaultTimeout(10_000);
    }

    @Bean("ioTaskExecutor")
    public Executor ioTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int processors = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(processors * 2);
        executor.setMaxPoolSize(processors * 4);
        executor.setQueueCapacity(64);
        executor.setThreadNamePrefix("IO-Task-");
        configureResilientExecutor(executor);

        executor.initialize();
        ExecutorServiceMetrics.monitor(meterRegistry, executor.getThreadPoolExecutor(), "io-task-executor");
        return executor;
    }

    @Bean("cpuTaskExecutor")
    public Executor cpuTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        int processors = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(processors + 1);
        executor.setMaxPoolSize(processors + 1);
        executor.setQueueCapacity(128);
        executor.setThreadNamePrefix("CPU-Task-");
        configureResilientExecutor(executor);

        executor.initialize();
        ExecutorServiceMetrics.monitor(meterRegistry, executor.getThreadPoolExecutor(), "cpu-task-executor");
        return executor;
    }

    @Bean("longTaskExecutor")
    public Executor longTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(5);
        executor.setQueueCapacity(10);
        executor.setKeepAliveSeconds(300);
        executor.setThreadNamePrefix("Long-Task-");
        configureResilientExecutor(executor);
        executor.initialize();
        ExecutorServiceMetrics.monitor(meterRegistry, executor.getThreadPoolExecutor(), "long-task-executor");
        return executor;
    }

    @Bean
    public ThreadPoolTaskExecutor mvcTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(16);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("mvc-async-");
        configureResilientExecutor(executor);
        executor.initialize();
        ExecutorServiceMetrics.monitor(meterRegistry, executor.getThreadPoolExecutor(), "mvc-async-executor");
        return executor;
    }

    private void configureResilientExecutor(ThreadPoolTaskExecutor executor) {
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
    }
}
