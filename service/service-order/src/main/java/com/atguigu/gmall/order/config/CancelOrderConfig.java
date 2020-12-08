package com.atguigu.gmall.order.config;

import com.atguigu.gmall.rabbit.constants.MQConst;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @title: CancelOrderConfig
 * @Author LiuXianKun
 * @Date: 2020/12/5 20:53
 */
@Configuration
public class CancelOrderConfig {

    @Bean //    //创建 Queue1
    public Queue queue1() {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", MQConst.EXCHANGE_DIRECT_ORDER_CANCEL);
        arguments.put("x-dead-letter-routing-key", MQConst.ROUTING_ORDER_CANCEL2);
        arguments.put("x-message-ttl", 60 * 1000);//过期时间 1分钟  全局时间
        return QueueBuilder.durable(MQConst.QUEUE_ORDER_CANCEL1).withArguments(arguments).build();
    }

    @Bean
    public Queue queue2() {
        return QueueBuilder.durable(MQConst.QUEUE_ORDER_CANCEL2).build();
    }

    @Bean
    public Exchange exchange() {
        return ExchangeBuilder.directExchange(MQConst.EXCHANGE_DIRECT_ORDER_CANCEL).build();
    }

    @Bean
    public Binding binding1() {
        return BindingBuilder.bind(queue1()).to(exchange()).
                with(MQConst.ROUTING_ORDER_CANCEL1).noargs();
    }

    @Bean
    public Binding binding2() {
        return BindingBuilder.bind(queue2()).to(exchange()).
                with(MQConst.ROUTING_ORDER_CANCEL2).noargs();
    }

}
