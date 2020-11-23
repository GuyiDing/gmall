package com.atguigu.gmall.common.pool;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @title: ThreadPool
 * @Author LiuXianKun
 * @Date: 2020/11/22 21:02
 */
@Configuration
public class ThreadPool {

    @Bean
    public ThreadPoolExecutor getThreadPool() {
        return new ThreadPoolExecutor(5, 10, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(10));
    }
}
