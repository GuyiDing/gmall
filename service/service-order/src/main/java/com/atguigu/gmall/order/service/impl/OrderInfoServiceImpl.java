package com.atguigu.gmall.order.service.impl;

import com.atguigu.gmall.common.util.HttpClientUtil;
import com.atguigu.gmall.model.cart.CartInfo;
import com.atguigu.gmall.model.enums.OrderStatus;
import com.atguigu.gmall.model.enums.ProcessStatus;
import com.atguigu.gmall.model.order.OrderDetail;
import com.atguigu.gmall.model.order.OrderInfo;
import com.atguigu.gmall.order.mapper.CartInfoMapper;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @title: OrderInfoServiceImpl
 * @Author LiuXianKun
 * @Date: 2020/12/3 17:36
 */
public class OrderInfoServiceImpl implements OrderInfoService {

    @Value("${ware.url}")
    private String url;
    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private OrderInfoMapper orderInfoMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Override
    public Boolean getStock(OrderDetail orderDetail) {

        return "1".equals(HttpClientUtil.doGet(url + "/hasStock?skuId=" + orderDetail.getSkuId() + "&num=" + orderDetail.getSkuNum()));

    }

    @Override
    public Long saveOrderInfo(OrderInfo orderInfo) {
        orderInfo.setOrderStatus(OrderStatus.UNPAID.name());  //getComment 中文
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);

        Date date = new Date();
        Calendar instance = Calendar.getInstance();
        instance.add(Calendar.HOUR, 2);  //两小时后过期
        Date expireTime = instance.getTime();
        orderInfo.setCreateTime(date);
        orderInfo.setExpireTime(expireTime);
        //进度状态
        orderInfo.setProcessStatus(ProcessStatus.UNPAID.name());
        StringBuilder sb = new StringBuilder();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            sb.append(orderDetail.getSkuNum() + " ");
            //真实价格
            orderDetail.setOrderPrice(productFeignClient.getPrice(orderDetail.getSkuId()));
        }
        if (sb.length() > 100) {
            orderInfo.setTradeBody(sb.toString().substring(0, 100));
        } else {
            orderInfo.setTradeBody(sb.toString());
        }

        //订单总金额的计算
        orderInfo.sumTotalAmount();


        QueryWrapper<CartInfo> cartInfoQueryWrapper = new QueryWrapper<>();
        //保存订单
        orderInfoMapper.insert(orderInfo);

        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insert(orderDetail);
            cartInfoQueryWrapper.eq("sku_id", orderDetail.getSkuId()).eq("user_id", orderInfo.getUserId());
            cartInfoQueryWrapper.or();
        }


        //3:删除购物车中提交了订单的商品
        // delete from 表 where  (sku_id = 10 and  user_id = 3 ) or (sku_id = 14 and  user_id = 3 ) or
        cartInfoMapper.delete(cartInfoQueryWrapper);

        // TODO: 2020/12/3 RabbitMQ
        return null;
    }
}
