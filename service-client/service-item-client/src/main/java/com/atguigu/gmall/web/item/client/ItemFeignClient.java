package com.atguigu.gmall.web.item.client;

import com.atguigu.gmall.web.item.client.impl.ItemDegradeFeignClient;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(value = "service-item", fallback = ItemDegradeFeignClient.class)
public interface ItemFeignClient {

    /**
     * @param skuId
     * @return
     * 数据汇总
     */
    @GetMapping("/api/item/{skuId}")
    Map<String,Object> getItem(@PathVariable("skuId") Long skuId);

}
