package com.org.ticketmaster.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    /**
     * Dedicated pool for post-commit notification work (SQS enqueue, etc.), kept
     * separate from request-handling threads. Spring's @Async default
     * (SimpleAsyncTaskExecutor) spawns an unbounded new thread per task, which
     * would let a slow/unavailable SQS endpoint take down the JVM under load -
     * a bounded pool with a queue turns that into backpressure instead.
     */
    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(50);
        executor.setQueueCapacity(1000);
        executor.setThreadNamePrefix("booking-notify-");
        executor.initialize();
        return executor;
    }
}