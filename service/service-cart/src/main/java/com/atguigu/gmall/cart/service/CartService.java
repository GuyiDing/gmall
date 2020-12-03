package com.atguigu.gmall.cart.service;

import com.atguigu.gmall.model.cart.CartInfo;

import java.util.List;

public interface CartService {
    CartInfo addToCart(Long skuId, Integer skuNum, String userId);


    CartInfo toCart(Long skuId, Integer skuNum,String userId);

    List<CartInfo> getCartList(String userId,String userTempId);

    void checkedCart(Long skuId, Integer isChecked, String userId);

    List<CartInfo> getCartCheckedList(String userId);

    void deleteCart(Long skuId, String userId);
}
