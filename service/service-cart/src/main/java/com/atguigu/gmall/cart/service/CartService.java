package com.atguigu.gmall.cart.service;

public interface CartService {
    void addToCart(Long skuId, Integer skuNum, String userId);
}
