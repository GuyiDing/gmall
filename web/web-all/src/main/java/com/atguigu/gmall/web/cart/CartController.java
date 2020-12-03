package com.atguigu.gmall.web.cart;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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
    public ModelAndView addCart(Long skuId, Integer skuNum, RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("skuId",skuId);
        redirectAttributes.addAttribute("skuNum",skuNum);
        cartFeignClient.addToCart(skuId, skuNum);
        return new ModelAndView("redirect:http://cart.gmall.com/toCart");
    }


    @GetMapping("/toCart")
    public String toCart(Long skuId,Integer skuNum, HttpServletRequest request) {
        CartInfo cartInfo = cartFeignClient.toCart(skuId,skuNum);
        request.setAttribute("cartInfo", cartInfo);
        return "cart/addCart";
    }

    //去购物车结算
    @GetMapping("/cart.html")
    public String cart(){
        //购物车页面
        return "cart/index";
    }
}
