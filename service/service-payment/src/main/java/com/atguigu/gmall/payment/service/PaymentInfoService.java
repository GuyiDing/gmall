package com.atguigu.gmall.payment.service;

import com.atguigu.gmall.model.payment.PaymentInfo;

import java.util.Map;

public interface PaymentInfoService {
    PaymentInfo getPaymentInfo(Long orderId, String name);

    void updateByOutTradeNo(Map<String, String> paramsMap);
}
