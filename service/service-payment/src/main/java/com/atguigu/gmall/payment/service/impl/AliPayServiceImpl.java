package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeCloseRequest;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.response.AlipayTradeCloseResponse;
import com.alipay.api.response.AlipayTradePagePayResponse;
import com.atguigu.gmall.model.enums.PaymentType;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AliPayService;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @title: AliPayServiceImpl
 * @Author LiuXianKun
 * @Date: 2020/12/7 10:08
 */
@Service
public class AliPayServiceImpl implements AliPayService {
    @Resource
    private PaymentInfoService paymentInfoService;
    @Autowired
    private AlipayClient alipayClient;

    @Override
    public String alipaySubmit(Long orderId) {

        //1:先生成流水（支付表）
        PaymentInfo paymentInfo = paymentInfoService.getPaymentInfo(orderId, PaymentType.ALIPAY.name());

        //2:调用统一收单下单并支付页面接口  返回值 String类型  页面 （支付页面） 扫码
        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        Map map = new HashMap<>();
        map.put("out_trade_no",paymentInfo.getOutTradeNo());
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",paymentInfo.getTotalAmount());
        map.put("subject",paymentInfo.getSubject());
        request.setBizContent(JSONObject.toJSONString(map));
        //同步回调地址
        request.setReturnUrl(AlipayConfig.return_payment_url);
        //异步回调地址
        request.setNotifyUrl(AlipayConfig.notify_payment_url);
        //发送
        AlipayTradePagePayResponse response = null;
        try {
            //向支付宝服务发出请求
            response = alipayClient.pageExecute(request);
            //获取需提交的form表单
            if(response.isSuccess()){
                String submitFormData = response.getBody();
                System.out.println(submitFormData);
                return submitFormData;
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void closePay(String outTradeNo) {
        AlipayTradeCloseRequest request = new AlipayTradeCloseRequest();
        Map map = new HashMap();
        map.put("out_trade_no",outTradeNo);
        request.setBizContent(JSONObject.toJSONString(map));
        AlipayTradeCloseResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功" + response.getBody());
        } else {
            System.out.println("调用失败" + response.getBody());
        }
    }
}
