package com.atguigu.gmall.rabbit.config;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.rabbit.constants.MQConst;
import com.atguigu.gmall.rabbit.entity.GmallCorrelationData;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

/**
 * @title: MQProducerAckConfig
 * @Author LiuXianKun
 * @Date: 2020/12/4 16:30
 */

@Component
//生产者消息丢失处理
public class MQProducerAckConfig implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisTemplate redisTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
        rabbitTemplate.setReturnCallback(this);
    }

    //生产者与交换机之间的消息丢失处理
    //消息确认机制  不管失败与否都需要回应一下
    //correlationData 关联消息发送者的信息
    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            System.out.println("交换机处理成功");  //应该写入日志  @sl4j
        } else {
            System.out.println("交换机处理有误，cause" + cause);
//            重新发送请求
            retrySendMessage(correlationData);
        }


    }

    private void retrySendMessage(CorrelationData correlationData) {
        GmallCorrelationData gmallCorrelationData = (GmallCorrelationData) correlationData;
        int retryCount = gmallCorrelationData.getRetryCount();
        if (retryCount < 2) {  //重试2次 总计3次
            retryCount++;
            gmallCorrelationData.setRetryCount(retryCount);

            //更新缓存
            redisTemplate.opsForHash().put(MQConst.EXCHANGE_KEY_REDIS, gmallCorrelationData.getId(), JSONObject.toJSONString(correlationData));

            //再次发送消息
            rabbitTemplate.convertAndSend(gmallCorrelationData.getExchange(),
                    gmallCorrelationData.getRoutingKey(),
                    gmallCorrelationData.getMessage(),
                    gmallCorrelationData);
        } else {
            System.out.println("已经尝试" + retryCount + "次了，不在给交换机发送消息");
        }


    }

    //交换机与队列之间的消息丢失处理  //只能是消息发送失败了才应答
    @Override
    public void returnedMessage(Message message, int replyCode, String replyText, String exchange, String routingKey) {
        // 反序列化对象输出
        System.out.println("消息主体: " + new String(message.getBody()));
        System.out.println("应答码: " + replyCode);
        System.out.println("描述：" + replyText);
        System.out.println("消息使用的交换器 exchange : " + exchange);
        System.out.println("消息使用的路由键 routing : " + routingKey);
        String uuid = message.getMessageProperties().getHeader("spring_returned_message_correlation");
        if (null != uuid) {
            return;
        }
        Object o = redisTemplate.opsForHash().get(MQConst.EXCHANGE_KEY_REDIS, uuid);
        if (o == null) {
            return;
        }
        retrySendMessage((GmallCorrelationData) o);
    }
}
