package com.atguigu.gmall.order.service;


import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;

public interface OrderInfoService {
    Boolean getStock(OrderDetail orderDetail);


    Long saveOrderInfo(OrderInfo orderInfo);
}
