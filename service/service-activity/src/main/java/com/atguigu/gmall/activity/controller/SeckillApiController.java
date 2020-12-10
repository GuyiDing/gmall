package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.model.activity.SeckillGoods;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/seckill")
public class SeckillApiController {

    @Autowired
    private SeckillGoodsService seckillGoodsService;
    //查询所有今天的秒杀商品
    @GetMapping
    public List<SeckillGoods> getSeckillGoodsList(){
        return seckillGoodsService.getSeckillGoodsList();
    }

    @GetMapping("/{skuId}")
    public SeckillGoods getSeckillGoodsBySkuId(@PathVariable Long skuId){
        return seckillGoodsService.getSeckillGoodsBySkuId(skuId);
    }
}

