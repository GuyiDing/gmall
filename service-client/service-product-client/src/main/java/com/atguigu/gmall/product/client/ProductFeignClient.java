package com.atguigu.gmall.product.client;

import com.atguigu.gmall.product.client.impl.ProductFeignClientImpl;
import org.springframework.cloud.openfeign.FeignClient;

@FeignClient(value = "service-product", fallback = ProductFeignClientImpl.class)
public interface ProductFeignClient {



}
