package com.atguigu.gmall.rabbit.constants;


public class MQConst {

    /**
     * 商品上下架
     */
    public static final String EXCHANGE_DIRECT_GOODS = "exchange.direct.goods";
    public static final String ROUTING_GOODS_UPPER = "goods.upper";
    public static final String ROUTING_GOODS_LOWER = "goods.lower";
    //队列
    public static final String QUEUE_GOODS_UPPER  = "queue.goods.upper";
    public static final String QUEUE_GOODS_LOWER  = "queue.goods.lower";

    //交换机在redis中的key
    public static final String EXCHANGE_KEY_REDIS  = "exchange.redis.key";
    public static final String EXCHANGE_KEY_REDIS_Delay  = "exchange.redis.delay.key";

    /**
     * 取消订单，发送延迟队列
     */
    public static final String EXCHANGE_DIRECT_ORDER_CANCEL = "exchange.direct.order.cancel";//"exchange.direct.order.create" test_exchange;
    public static final String ROUTING_ORDER_CANCEL1 = "order.create1";
    public static final String ROUTING_ORDER_CANCEL2 = "order.create2";
    //延迟取消订单队列
    public static final String QUEUE_ORDER_CANCEL1  = "queue.order.cancel1";
    public static final String QUEUE_ORDER_CANCEL2  = "queue.order.cancel2";
    //取消订单 延迟时间 单位：秒
    public static final int DELAY_TIME  = 60*60*2;


    /**
     * 订单支付  更新订单的支付成功状态
     */
    public static final String EXCHANGE_DIRECT_PAYMENT_PAY = "exchange.direct.payment.pay";
    public static final String ROUTING_PAYMENT_PAY = "payment.pay";
    //队列
    public static final String QUEUE_PAYMENT_PAY  = "queue.payment.pay";

    /**
     * 减库存
     */
    public static final String EXCHANGE_DIRECT_WARE_STOCK = "exchange.direct.ware.stock";
    public static final String ROUTING_WARE_STOCK = "ware.stock";
    //队列
    public static final String QUEUE_WARE_STOCK  = "queue.ware.stock";


    /**
     * 定时任务
     */
    public static final String EXCHANGE_DIRECT_TASK = "exchange.direct.task";
    public static final String ROUTING_TASK_1 = "seckill.task.1";
    //队列
    public static final String QUEUE_TASK_1  = "queue.task.1";
}
