package com.atguigu.gmall.order.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.service.OrderInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @title: OrderApiController
 * @Author LiuXianKun
 * @Date: 2020/12/3 17:34
 */


@RestController
@RequestMapping("api/order")
public class OrderApiController {

    @Autowired
    private RedisTemplate redisTemplate;
    @Resource
    private OrderInfoService orderInfoService;

    @GetMapping("/auth/getTradeNo")
    public String getTradeNo(HttpServletRequest request) {
        String userId = AuthContextHolder.getUserId(request);
        String tradeNo = UUID.randomUUID().toString().replaceAll("-", "");
        redisTemplate.opsForValue().set("tradeNo:" + userId, tradeNo);
        return tradeNo;
    }

    @PostMapping("auth/submitOrder")
    public Result submitOrder(HttpServletRequest request, @RequestBody OrderInfo orderInfo, String tradeNo) {
        //检查是否有订单号 订单号是否被修改
        String userId = AuthContextHolder.getUserId(request);
        String submitTradeNo = (String) redisTemplate.opsForValue().get("tradeNo:" + userId);
        if (StringUtils.isEmpty(submitTradeNo)) {
            return Result.fail().message("请勿重复提交该订单");
        } else if (!submitTradeNo.equals(tradeNo)) {
            return Result.fail().message("请勿非法操作");
        }
        redisTemplate.delete("tradeNo:" + userId);
        //判断是否还库存   有货或是无货
        //无货： 剩余数量小于购买数量

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Boolean ifHasStock = orderInfoService.getStock(orderDetail);
            if (!ifHasStock) {
                return Result.fail().message("缺货");
            }
        }
        //提交订单
        orderInfo.setUserId(Long.parseLong(userId));
        Long orderId = orderInfoService.saveOrderInfo(orderInfo);
        return Result.ok(orderId);
    }

    @GetMapping("/auth/getOrderInfo/{orderId}")
    public OrderInfo getOrderInfo(@PathVariable Long orderId) {
        return orderInfoService.getOrderInfo(orderId);
    }

    //由库存系统发起申请  折单接口
    // http://order.gmall.com/api/order/orderSplit?orderId=xxx&wareSkuMap=xxx
    @PostMapping("/orderSplit")
    public List<Map> orderSplit(Long orderId ,String wareSkuMap) {
        //wareSkuMap  [{"wareId":"1","skuIds":["1"]},{"wareId":"2","skuIds":["10","13"]}]
        //1:订单微服务就要开始折单   父订单 折成多个子订单
        List<OrderInfo> orderInfoList = orderInfoService.orderSplit(orderId, wareSkuMap);
        List<Map> listMap = new ArrayList<>();
        orderInfoList.forEach(orderInfo -> {
            Map<String, Object> map = orderInfoService.initWareOrder(orderInfo);
            listMap.add(map);
        });

        return listMap;
    }

}
