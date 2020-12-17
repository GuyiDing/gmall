package com.atguigu.gmall.order.service.impl;

import com.alibaba.fastjson.JSONObject;
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
import com.atguigu.gmall.rabbit.constants.MQConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @title: OrderInfoServiceImpl
 * @Author LiuXianKun
 * @Date: 2020/12/3 17:36
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
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

    @Autowired
    private RabbitService rabbitService;

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
        rabbitService.sendDelayMessage(MQConst.EXCHANGE_DIRECT_ORDER_CANCEL,
                MQConst.ROUTING_ORDER_CANCEL1,
                orderInfo.getId(),
                10);
        // TODO: 2020/12/7 先暂时取消MQ的发送  不要关闭订单
        return orderInfo.getId();
    }

    @Override
    public void cancelOrder(Long orderId) {
        //幂等性问题
        //1:查询订单状态 是否已经取消
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (orderInfo.getOrderStatus().equals(OrderStatus.UNPAID.name())) {
            orderInfo.setOrderStatus(OrderStatus.CLOSED.name());
            orderInfo.setProcessStatus(ProcessStatus.CLOSED.name());
            orderInfo.setExpireTime(new Date());
            orderInfoMapper.updateById(orderInfo);
        }
    }

    @Override
    public OrderInfo getOrderInfo(Long orderId) {
        return orderInfoMapper.selectById(orderId);
    }

    //更新订单的状态  已经支付的状态
    @Override
    public void updateOrderStatus(Long orderId) {
        //幂待性问题
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        //判断是否为未支付
        if (OrderStatus.UNPAID.name().equals(orderInfo.getOrderStatus())) {
            //改成已支付
            orderInfo.setOrderStatus(OrderStatus.PAID.name());
            //进度状态
            orderInfo.setProcessStatus(ProcessStatus.PAID.name());
            //更新订单的状态
            orderInfoMapper.updateById(orderInfo);
            //发消息 减库存
            rabbitService.sendMessage(MQConst.EXCHANGE_DIRECT_WARE_STOCK,
                    MQConst.ROUTING_WARE_STOCK,
                    JSONObject.toJSONString(initWareOrder(orderId)));
        }
    }

    //查询订单及详情相关
    public OrderInfo getOrderInfoAndOrderDetail(Long orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectById(orderId);
        if (null != orderInfo) {
            orderInfo.setOrderDetailList(orderDetailMapper.selectList(new QueryWrapper<OrderDetail>()
                    .eq("order_id", orderId)));
        }
        return orderInfo;
    }

    @Override
    public List<OrderInfo> orderSplit(Long orderId, String wareSkuMap) {
        //[{"wareId":"1","skuIds":["1"]},{"wareId":"2","skuIds":["10","13"]}]
        List<Map> maps = JSONObject.parseArray(wareSkuMap, Map.class);
        //1:原始订单 为基本 折成多个子订单
        OrderInfo originOrderInfo = getOrderInfoAndOrderDetail(orderId);
        //  OrderInfo 订单主表  14(一个）  OrderDetail订单详情表  3条    skuId: 10 13 1
        /////////////////////////////////////////////////////////
        //  OrderInfo 订单主表  15      OrderDetail订单详情表  1条 skuID:1
        //  OrderInfo 订单主表  16      OrderDetail订单详情表  2条 skuID: 10 13
        List<OrderInfo> subOrderInfoList = new ArrayList<>();

        List<OrderDetail> orderDetailList = originOrderInfo.getOrderDetailList();
        maps.forEach(map -> {
            List<OrderDetail> subOrderDetailList = new ArrayList<>();
            OrderInfo subOrderInfo = new OrderInfo();
            List<String> skuIds = (List<String>) map.get("skuIds");
            ArrayList<Object> orderDetailIds = new ArrayList<>();
            orderDetailList.forEach(orderDetail -> {
                if (skuIds.contains(orderDetail.getSkuId())) {
                    orderDetailIds.add(orderDetail.getId());
                    subOrderDetailList.add(orderDetail);
                }
            });
            subOrderInfo.setOrderDetailList(subOrderDetailList);
            //计算子订单的总金额
            subOrderInfo.sumTotalAmount();
            //保存
            saveOrderInfoToDB(subOrderInfo, orderDetailIds);

            //折单 后的多个子订单主表及子订单详情集合）
            subOrderInfoList.add(subOrderInfo);
        });
        //更新原始订单的订单状态为已折单  进度状态为已折单
        updateOrderStatusForSplit(orderId, ProcessStatus.SPLIT);
        //返回值  List<OrderInfo>  OrderInfo  OrderDetailList
        return subOrderInfoList;
    }

    private void updateOrderStatusForSplit(Long orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = getOrderInfo(orderId);
        orderInfo.setOrderStatus(processStatus.getOrderStatus().name());
        orderInfo.setProcessStatus(processStatus.name());
        orderInfoMapper.updateById(orderInfo);
    }

    private void saveOrderInfoToDB(OrderInfo subOrderInfo, ArrayList<Object> orderDetailIds) {
        orderInfoMapper.insert(subOrderInfo);
        //保存子订单详情表集合  update order_detail set order_id = 15 where id in (33,34)
        OrderDetail subOrderDetail = new OrderDetail();
        subOrderDetail.setOrderId(subOrderInfo.getId());
        orderDetailMapper.update(subOrderDetail,
                new QueryWrapper<OrderDetail>().in("id", orderDetailIds));
    }

    //重载方法
    public Map<String, Object> initWareOrder(Long orderId) {
        //1:查询订单及订单详情
        OrderInfo orderInfo = getOrderInfoAndOrderDetail(orderId);
        //2:组合发送的消息内容
        Map<String, Object> result = initWareOrder(orderInfo);
        return result;
    }


    //发消息给库存系统 减库存的时候 组合发送的消息内容
    public Map<String, Object> initWareOrder(OrderInfo orderInfo) {
        Map<String, Object> result = new HashMap<>();
        //订单相关信息
        result.put("orderId", orderInfo.getId());
        result.put("consignee", orderInfo.getConsignee());
        result.put("consigneeTel", orderInfo.getConsigneeTel());
        result.put("orderComment", orderInfo.getOrderComment());
        result.put("orderBody", orderInfo.getTradeBody());
        result.put("deliveryAddress", orderInfo.getDeliveryAddress());
        result.put("paymentWay", "2");
        result.put("wareId", orderInfo.getWareId());
        //订单详情信息
        List<Map> details = new ArrayList<>();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        orderDetailList.forEach(orderDetail -> {
            Map map = new HashMap();
            map.put("skuId", orderDetail.getSkuId());
            map.put("skuNum", orderDetail.getSkuNum());
            map.put("skuName", orderDetail.getSkuName());
            details.add(map);
        });
        //将订单详情保存在订单的Map中
        result.put("details", details);
        return result;
    }
}
