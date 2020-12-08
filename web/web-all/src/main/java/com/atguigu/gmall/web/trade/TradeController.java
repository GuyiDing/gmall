package com.atguigu.gmall.web.trade;

import com.atguigu.gmall.cart.client.CartFeignClient;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.order.client.OrderFeignClient;
import com.atguigu.gmall.user.client.UserFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @title: TradeController
 * @Author LiuXianKun
 * @Date: 2020/12/3 14:30
 */
@Controller
@RequestMapping
public class TradeController {
    @Autowired
    private UserFeignClient userFeignClient;

    @Autowired
    private OrderFeignClient orderFeignClient;

    @Autowired
    private CartFeignClient cartFeignClient;

    @GetMapping("/trade.html")
    public String goToTradePage(Model model) {
        List<UserAddress> userAddressList = userFeignClient.getUserAddressList();
        model.addAttribute("userAddressList", userAddressList);
        String tradeNo = orderFeignClient.getTradeNo();
        model.addAttribute("tradeNo", tradeNo);
        List<CartInfo> cartCheckedList = cartFeignClient.getCartCheckedList();
        List<OrderDetail> orderDetailList = cartCheckedList.stream().map(cartInfo -> {
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setOrderPrice(cartInfo.getSkuPrice());
            return orderDetail;
        }).collect(Collectors.toList());
        model.addAttribute("detailArrayList", orderDetailList);
        long sum = cartCheckedList.stream().collect(Collectors.summarizingInt(CartInfo::getSkuNum)).getSum();
        model.addAttribute("total", sum);
        //求总金额  价格 * 件数
        BigDecimal totalAmount = new BigDecimal(0);
        for (CartInfo cartInfo : cartCheckedList) {
            totalAmount = totalAmount.add(cartInfo.getSkuPrice()).multiply(new BigDecimal(cartInfo.getSkuNum()));
        }
        model.addAttribute("totalAmount",totalAmount);
        return "/order/trade";
    }



}
