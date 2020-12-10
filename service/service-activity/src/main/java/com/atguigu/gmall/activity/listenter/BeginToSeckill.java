package com.atguigu.gmall.activity.listenter;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.activity.OrderRecode;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.rabbit.constants.MQConst;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @title: BeginToSeckill
 * @Author LiuXianKun
 * @Date: 2020/12/10 18:07
 */
@Component
@Slf4j
public class BeginToSeckill {
    @Resource
    private SeckillGoodsService seckillGoodsService;
    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_SECKILL_USER, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MQConst.EXCHANGE_DIRECT_SECKILL_USER),
            key = MQConst.ROUTING_SECKILL_USER
    ))
    public void seckillGoods(UserRecode userRecode, Message message, Channel channel) {
        try {
            //        1、校验是否有库存（内存）
            Object o = CacheHelper.get(userRecode.getSkuId().toString());
            if (null == o || !"1".equals(o)) {
            }
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
//            2、校验用户是否已经购买过了
            Boolean ifAbsent = redisTemplate.opsForValue().setIfAbsent(RedisConst.SECKILL_USER +
                    userRecode.getUserId(), "");  //setNX 如果没有购买过则添加 如果购买过返回false
            if (!ifAbsent) {
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
//            3、校验rightPop  Redis   更新内存中库
            Object rightPop = redisTemplate.opsForList().rightPop(RedisConst.SECKILL_STOCK_PREFIX +
                    userRecode.getSkuId());
            if (null == rightPop) {
                //已售完
                redisTemplate.convertAndSend("seckillpush",
                        userRecode.getSkuId() + ":0");
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
                return;
            }
//            4、在Redis中保存抢购资格
            OrderRecode orderRecode = new OrderRecode();//下单资格  抢购成功
            redisTemplate.opsForValue().set(RedisConst.SECKILL_ORDERS +
                    userRecode.getUserId(), orderRecode);
            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            //e.printStackTrace();
            log.error(ExceptionUtils.getStackTrace(e));
        }

    }
}
