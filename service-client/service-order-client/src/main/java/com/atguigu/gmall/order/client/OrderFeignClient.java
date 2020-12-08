package com.atguigu.gmall.order.client;

import com.atguigu.gmall.model.order.OrderInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * @title: OrderFeignClient
 * @Author LiuXianKun
 * @Date: 2020/12/3 17:41
 */
@FeignClient("service-order")
public interface OrderFeignClient {

    @GetMapping("api/order/auth/getTradeNo")
    String getTradeNo();

    @GetMapping("api/order/auth/getOrderInfo/{orderId}")
    OrderInfo getOrderInfo(@PathVariable Long orderId);
}
