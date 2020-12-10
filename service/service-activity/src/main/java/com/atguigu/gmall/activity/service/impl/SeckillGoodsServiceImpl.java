package com.atguigu.gmall.activity.service.impl;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @title: SeckillGoodsServiceImpl
 * @Author LiuXianKun
 * @Date: 2020/12/10 17:52
 */
@Service
public class SeckillGoodsServiceImpl implements SeckillGoodsService {

    //缓存
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public SeckillGoods getSeckillGoodsBySkuId(Long skuId) {
        return (SeckillGoods) redisTemplate.opsForHash().
                get(RedisConst.SECKILL_GOODS, skuId.toString());
    }


    @Override
    public List<SeckillGoods> getSeckillGoodsList() {
        return redisTemplate.opsForHash().values(RedisConst.SECKILL_GOODS);
    }
}
