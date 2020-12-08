package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.model.enums.PaymentStatus;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.model.payment.PaymentInfo;
import com.atguigu.gmall.payment.mapper.OrderInfoMapper;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import com.atguigu.gmall.rabbit.constants.MQConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;
import java.util.Map;

/**
 * @title: PaymentInfoServiceImpl
 * @Author LiuXianKun
 * @Date: 2020/12/7 11:14
 */
@Service
public class PaymentInfoServiceImpl implements PaymentInfoService {
    @Resource
    private PaymentInfoMapper paymentInfoMapper;
    @Resource
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private RabbitService rabbitService;

    @Override
    public PaymentInfo getPaymentInfo(Long orderId, String name) {

        //1:查询当前订单ID对应的支付信息
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(new
                QueryWrapper<PaymentInfo>().eq("order_id", orderId));
        if (null == paymentInfo) {
            //2:不存在  生成支付信息
            paymentInfo = new PaymentInfo();
            OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
            paymentInfo.setOrderId(orderId);
            paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
            paymentInfo.setPaymentType(name);
            paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
            paymentInfo.setSubject(orderInfo.getTradeBody());
            paymentInfo.setPaymentStatus(PaymentStatus.UNPAID.name());
            paymentInfo.setCreateTime(new Date());
            paymentInfoMapper.insert(paymentInfo);
        }
        return paymentInfo;
    }
    //更新支付表相关状态及数据
    @Override
    public void updateByOutTradeNo(Map<String, String> paramsMap) {
        PaymentInfo paymentInfo = paymentInfoMapper.selectOne(new QueryWrapper<PaymentInfo>().eq("out_trade_no", paramsMap.get("out_trade_no")));
        if (null != paymentInfo && paymentInfo.getPaymentStatus().equals(PaymentStatus.UNPAID.name())) {
            paymentInfo.setPaymentStatus(PaymentStatus.PAID.name());
            //trade_no
            paymentInfo.setTradeNo(paramsMap.get("trade_no"));
            //回调时间
            paymentInfo.setCallbackTime(new Date());
            //回调
            paymentInfo.setCallbackContent(JSONObject.toJSONString(paramsMap));
            paymentInfoMapper.updateById(paymentInfo);

            //Feign 不适合的  MQ 保存事务问题
            rabbitService.sendMessage(MQConst.EXCHANGE_DIRECT_PAYMENT_PAY,
                    MQConst.ROUTING_PAYMENT_PAY,paymentInfo.getOrderId());
        }
    }
}
