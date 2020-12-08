package com.atguigu.gmall.rabbit.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.rabbit.constants.MQConst;
import com.atguigu.gmall.rabbit.entity.GmallCorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * @title: RabbitService
 * @Author LiuXianKun
 * @Date: 2020/12/4 16:22
 */
@SuppressWarnings("Duplicates")
@Component
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    //发送消息
    public void sendMessage(String exchange, String routingKey, Object message) {
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        String uuid = UUID.randomUUID().toString();
        gmallCorrelationData.setId(uuid);
        //交换机
        gmallCorrelationData.setExchange(exchange);
        //路由Key
        gmallCorrelationData.setRoutingKey(routingKey);
        //消息
        gmallCorrelationData.setMessage(message);

        redisTemplate.opsForHash().put(MQConst.EXCHANGE_KEY_REDIS, uuid, JSONObject.toJSON(gmallCorrelationData));
        rabbitTemplate.convertAndSend(exchange, routingKey, message, gmallCorrelationData);
    }


    //发送延迟消息（通过死信交换机完成该功能）
    public void sendDelayMessage(String exchange, String routingKey, Object message, int delayTime) {
        GmallCorrelationData gmallCorrelationData = new GmallCorrelationData();
        String uuid = UUID.randomUUID().toString();
        gmallCorrelationData.setId(uuid);
        //交换机
        gmallCorrelationData.setExchange(exchange);
        //路由Key
        gmallCorrelationData.setRoutingKey(routingKey);
        //消息
        gmallCorrelationData.setMessage(message);
        //设置延迟时间
        gmallCorrelationData.setDelay(true);
        gmallCorrelationData.setDelayTime(delayTime);// TODO: 2020/12/8 2020年12月8日22:00:42 发现未知错误
        redisTemplate.opsForHash().put(MQConst.EXCHANGE_KEY_REDIS_Delay, uuid, JSONObject.toJSON(gmallCorrelationData));
        rabbitTemplate.convertAndSend(exchange, routingKey, message, message1 -> {
            message1.getMessageProperties().setDelay(delayTime * 1000);
            return message1;
        }, gmallCorrelationData);

    }
}
