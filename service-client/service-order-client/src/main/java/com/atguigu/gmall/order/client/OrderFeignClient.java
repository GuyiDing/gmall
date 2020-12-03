package com.atguigu.gmall.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @title: OrderFeignClient
 * @Author LiuXianKun
 * @Date: 2020/12/3 17:41
 */
@FeignClient("service-order")
public interface OrderFeignClient {

    @GetMapping("api/order/auth/getTradeNo")
    String getTradeNo();
}
