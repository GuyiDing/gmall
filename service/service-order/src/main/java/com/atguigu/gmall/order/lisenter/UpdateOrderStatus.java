package com.atguigu.gmall.order.lisenter;

import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.rabbit.constants.MQConst;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @title: UpdateOrderStatus
 * @Author LiuXianKun
 * @Date: 2020/12/7 21:13
 */
@Component
public class UpdateOrderStatus {
    @Resource
    private OrderInfoService orderInfoService;
    //更新订单 支付成功状态
    @RabbitListener(bindings = {
            @QueueBinding(
                    exchange = @Exchange(value = MQConst.EXCHANGE_DIRECT_PAYMENT_PAY),
                    value = @Queue(value = MQConst.QUEUE_PAYMENT_PAY, durable = "true"
                            , autoDelete = "false"),
                    key = MQConst.ROUTING_PAYMENT_PAY
            )
    })
    public void updateOrderStatus(Long orderId, Channel channel, Message message) {
        try {
            System.out.println(new Date() + ":" + orderId);
            //更新订单的状态  已经支付的状态
            orderInfoService.updateOrderStatus(orderId);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
