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
@Component
public class RabbitService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    //发送消息
    public void sendMessage(String exchange,String routingKey,Object message) {
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
        rabbitTemplate.convertAndSend(exchange,routingKey,message,gmallCorrelationData);
    }

}
