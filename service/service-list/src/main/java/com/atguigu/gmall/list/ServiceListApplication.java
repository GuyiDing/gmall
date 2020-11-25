package com.atguigu.gmall.list;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * @title: ServiceListApplication
 * @Author LiuXianKun
 * @Date: 2020/11/23 20:34
 */
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atguigu.gmall")
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class, scanBasePackages = "com.atguigu.gmall")
public class ServiceListApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceListApplication.class);
    }

}
