package com.atguigu.gmall.payment.service;

public interface AliPayService {

    String alipaySubmit(Long orderId);

    void closePay(String outTradeNo);
}
