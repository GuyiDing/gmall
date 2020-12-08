package com.atguigu.gmall.cart.controller;

import com.atguigu.gmall.cart.service.CartService;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @title: CartApiController
 * @Author LiuXianKun
 * @Date: 2020/11/29 21:10
 */
@RestController
@RequestMapping("api/cart")
public class CartApiController {
    @Autowired
    private CartService cartService;

    @PostMapping("addToCart/{skuId}/{skuNum}")
    public Result addToCart(@PathVariable("skuId") Long skuId,
                          @PathVariable("skuNum") Integer skuNum,
                          HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        CartInfo cartInfo = cartService.addToCart(skuId, skuNum, userId);
        return Result.ok(cartInfo.getSkuNum()).message("添加成功");
    }


    @GetMapping("toCart/{skuId}/{skuNum}")
    public CartInfo toCart(@PathVariable("skuId") Long skuId, @PathVariable("skuNum") Integer skuNum, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        return cartService.toCart(skuId, skuNum, userId);
    }

    @GetMapping("/cartList")
    public Result cartInfoList(HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        String userTempId = AuthContextHolder.getUserTempId(request);
        List<CartInfo> cartList = cartService.getCartList(userId, userTempId);
        return Result.ok(cartList);
    }

    @GetMapping("/checkCart/{skuId}/{isChecked}")
    public Result checkedCart(@PathVariable Long skuId, @PathVariable Integer isChecked, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.checkedCart(skuId, isChecked, userId);
        return Result.ok();
    }

    //获取商品清单  结算  必须登录
    @GetMapping("/getCartCheckedList")
    public List<CartInfo> getCartCheckedList(HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        return cartService.getCartCheckedList(userId);
    }

    //删除购物车
    @DeleteMapping("/deleteCart/{skuId}")
    public Result deleteCart(@PathVariable Long skuId, HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        if (StringUtils.isEmpty(userId)) {
            userId = AuthContextHolder.getUserTempId(request);
        }
        cartService.deleteCart(skuId,userId);
        return Result.ok().message("删除成功");
    }


}
