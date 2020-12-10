package com.atguigu.gmall.activity.service;

import com.atguigu.gmall.model.activity.SeckillGoods;

import java.util.List;

public interface SeckillGoodsService {
    SeckillGoods getSeckillGoodsBySkuId(Long skuId);

    List<SeckillGoods> getSeckillGoodsList();
}
