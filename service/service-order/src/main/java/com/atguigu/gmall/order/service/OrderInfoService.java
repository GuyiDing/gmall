package com.atguigu.gmall.order.service;


import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;

import java.util.List;
import java.util.Map;

public interface OrderInfoService {
    Boolean getStock(OrderDetail orderDetail);


    Long saveOrderInfo(OrderInfo orderInfo);

    void cancelOrder(Long orderId);

    OrderInfo getOrderInfo(Long orderId);

    void updateOrderStatus(Long orderId);

    //发消息给库存系统 减库存的时候 组合发送的消息内容
    Map<String, Object> initWareOrder(OrderInfo orderInfo);

    //重载方法
    Map<String, Object> initWareOrder(Long orderId);

    //查询订单及详情相关
    OrderInfo getOrderInfoAndOrderDetail(Long orderId);

    List<OrderInfo> orderSplit(Long orderId, String wareSkuMap);
}
