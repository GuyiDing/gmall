package com.atguigu.gmall.activity.listenter;

import com.atguigu.gmall.activity.mapper.SeckillGoodsMapper;
import com.atguigu.gmall.activity.utils.DateUtil;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.rabbit.constants.MQConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @title: DBUpdateToRedis
 * @Author LiuXianKun
 * @Date: 2020/12/8 21:08
 */
@Component
@Slf4j
public class DBUpdateToRedis {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitService rabbitService;
    @Resource
    private SeckillGoodsMapper seckillGoodsMapper;

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = MQConst.QUEUE_TASK_1, durable = "true", autoDelete = "false"),
            exchange = @Exchange(value = MQConst.EXCHANGE_DIRECT_TASK),
            key = MQConst.ROUTING_TASK_1
    ))
    public void SyncToRedis(Channel channel, Message message) {
        try {
            QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("status", "1").
                    eq("DATE_FORMAT(start_time,'%Y-%m-%d')", DateUtil.formatDate(new Date()));
            queryWrapper.gt("stock_count", 0);
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(queryWrapper);
            if (!CollectionUtils.isEmpty(seckillGoodsList)) {
                seckillGoodsList.forEach(seckillGoods -> {
                    //2:保存到Redis 先判断redis中是否已经存在 不能覆盖了
                    if (!redisTemplate.opsForHash().hasKey(RedisConst.SECKILL_GOODS, seckillGoods.getSkuId().toString())) {
                        redisTemplate.opsForHash().put(RedisConst.SECKILL_GOODS, seckillGoods.getSkuId().toString(), seckillGoods);
                        //3：防止超卖 （秒杀核心内容）  List类型 防止超卖
                        redisTemplate.opsForList().leftPushAll(RedisConst.SECKILL_STOCK_PREFIX + seckillGoods.getSkuId(),
                                buildSkuIds(seckillGoods));
                    }

                });
            }

            channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);
        } catch (IOException e) {
            //e.printStackTrace();
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }

    private List<String> buildSkuIds(SeckillGoods seckillGoods) {
        List<String> list = new ArrayList<>();

        for (int i = 0; i < seckillGoods.getStockCount(); i++) {
            list.add(seckillGoods.getSkuId().toString());
        }
        return list;
    }

}
