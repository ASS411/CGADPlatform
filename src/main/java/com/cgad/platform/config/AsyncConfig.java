package com.cgad.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 异步任务配置
 *
 * Spring @Async 的原理：
 *   - @Async 注解标记的方法会在独立线程中执行，不阻塞调用方
 *   - 底层使用线程池管理异步任务的执行
 *   - 适用于大文件处理、文档向量化等耗时操作
 *
 * 为什么需要异步化？
 *   - 大文件解析可能耗时数十秒
 *   - 如果同步处理，HTTP 请求线程被阻塞，影响系统吞吐量
 *   - 异步化后，接口立即返回任务 ID，前端轮询查询进度
 */
@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean("documentTaskExecutor")
    public Executor documentTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("doc-async-");
        executor.initialize();
        return executor;
    }
}
