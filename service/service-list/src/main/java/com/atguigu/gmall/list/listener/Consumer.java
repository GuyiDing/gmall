package com.atguigu.gmall.list.listener;

import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.rabbit.constants.MQConst;
import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @title: Consumer
 * @Author LiuXianKun
 * @Date: 2020/12/4 18:14
 */
@SuppressWarnings("Duplicates")
@Component
public class Consumer {
    @Autowired
    private ListService listService;

    @RabbitListener(bindings = @QueueBinding(exchange = @Exchange(value = MQConst.EXCHANGE_DIRECT_GOODS),
            value = @Queue(value = MQConst.QUEUE_GOODS_UPPER, durable = "true", autoDelete = "false"),
            key = MQConst.ROUTING_GOODS_UPPER))
    public void onSale(Long skuId, Channel channel, Message message) {
        listService.onSale(skuId);
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);  //批量确认
        } catch (IOException e) {
            e.printStackTrace();
            //判断消息是否为重新发送
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
                            , false, true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    public void cancel(Long skuId, Channel channel, Message message) {
        listService.cancel(skuId);
        try {
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);  //批量确认
        } catch (IOException e) {
            e.printStackTrace();
            //判断消息是否为重新发送
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
                    channel.basicNack(message.getMessageProperties().getDeliveryTag(),
                            false,
                            true);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }
}
