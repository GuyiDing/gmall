package com.atguigu.gmall.cart.client;


import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "service-cart")
public interface CartFeignClient {

    @PostMapping("/api/cart/addToCart/{skuId}/{skuNum}")
    CartInfo addToCart(@PathVariable("skuId") Long skuId,
                   @PathVariable("skuNum") Integer skuNum);

    @GetMapping("/api/cart/toCart/{skuId}/{skuNum}")
    CartInfo toCart(@PathVariable("skuId") Long skuId,
                    @PathVariable("skuNum") Integer skuNum);

}
