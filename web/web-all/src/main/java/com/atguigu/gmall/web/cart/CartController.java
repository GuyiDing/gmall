package com.atguigu.gmall.web.cart;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;

/**
 * @title: CartController
 * @Author LiuXianKun
 * @Date: 2020/11/30 11:09
 */
@Controller
@RequestMapping
public class CartController {

    @Autowired
    private CartFeignClient cartFeignClient;

    @GetMapping("/addCart.html")
    public String addCart(Long skuId, Integer skuNum, HttpServletRequest request) {
        CartInfo cartInfo = cartFeignClient.addToCart(skuId, skuNum);
        request.setAttribute("cartInfo",cartInfo);
        return "cart/addCart";
    }
}
