package com.atguigu.gmall.order.lisenter;

import com.atguigu.gmall.order.service.OrderInfoService;
import com.atguigu.gmall.rabbit.constants.MQConst;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;

/**
 * @title: CancelOrder
 * @Author LiuXianKun
 * @Date: 2020/12/6 12:56
 */
@SuppressWarnings("Duplicates")
@Component
public class CancelOrderConsumer {

    @Resource
    private OrderInfoService orderInfoService;


    @RabbitListener(queues = MQConst.QUEUE_ORDER_CANCEL2)
    public void CancelOrder(Long orderId, Channel channel, Message message) {

        System.out.printf(new Date() + ":" + orderId);
        orderInfoService.cancelOrder(orderId);
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            if (message.getMessageProperties().getRedelivered()) {
                //已经是重发的消息  不让再发了
                //参数1： 消息的标记
                //参数2：是否放回队列   true 表示放回队列  false : 不放回
                try {
                    channel.basicReject(message.getMessageProperties().getDeliveryTag()
                            , false);//不入队
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } else {
                //是新的消息   可以重发一次
                //参数1： 消息的标记
                //参数2：是否批量
                //参数3：是否放回队列   true 表示放回队列  false : 不放回
                try {
                    channel.basicNack(message.getMessageProperties().getDeliveryTag()
                            ,false,true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }


}
