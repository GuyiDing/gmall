//package com.atguigu.gmall.activity.client;
//
//import com.atguigu.gmall.model.activity.SeckillGoods;
//import org.springframework.cloud.openfeign.FeignClient;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//import java.util.List;
//
///**
// * @Author: lx
// * @Time: 9:26
// * @Description:
// */
//@FeignClient(name = "service-activity")
//public interface SeckillFeignClient {
//
//    //查询所有今天的秒杀商品
//    @GetMapping("/api/seckill")
//    List<SeckillGoods> getSeckillGoodsList();
//
//    @GetMapping("/api/seckill/{skuId}")
//    SeckillGoods getSeckillGoodsBySkuId(@PathVariable Long skuId);
//}
