package com.atguigu.gmall.web.pay;

import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.client.OrderFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @title: PayController
 * @Author LiuXianKun
 * @Date: 2020/12/6 15:11
 */
@Controller
public class PayController {
    @Autowired
    private OrderFeignClient orderFeignClient;

    @GetMapping("/pay.html")
    public String payPage(Long orderId, Model model) {
        OrderInfo orderInfo = orderFeignClient.getOrderInfo(orderId);
        model.addAttribute("orderInfo", orderInfo);
        return "payment/pay";
    }

    //支付宝成功之后重定向过来的请求
    @GetMapping("/pay/success.html")
    public String paySuccess(){
        return "payment/success";
    }

}
