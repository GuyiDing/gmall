package com.atguigu.gmall.activity.controller;

import com.atguigu.gmall.activity.service.SeckillGoodsService;
import com.atguigu.gmall.activity.util.CacheHelper;
import com.atguigu.gmall.activity.utils.DateUtil;
import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.common.result.ResultCodeEnum;
import com.atguigu.gmall.common.util.AuthContextHolder;
import com.atguigu.gmall.common.util.MD5;
import com.atguigu.gmall.model.activity.SeckillGoods;
import com.atguigu.gmall.model.activity.UserRecode;
import com.atguigu.gmall.rabbit.constants.MQConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Date;

/**
 * @title: ActivityApiController
 * @Author LiuXianKun
 * @Date: 2020/12/10 17:49
 */

@RequestMapping("/api/activity/seckill")
@RestController
public class ActivityApiController {
    @Resource
    private SeckillGoodsService seckillGoodsService;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitService rabbitService;

    @GetMapping("/auth/getSeckillSkuStr/{skuId}")
    public Result getSeckillSkuStr(@PathVariable Long skuId, HttpServletRequest request) {
        SeckillGoods seckillGoods = seckillGoodsService.getSeckillGoodsBySkuId(skuId);
        String userId = AuthContextHolder.getUserId(request);
        //2:商品是否还有库存
        if (seckillGoods.getStockCount() == 0) {
            return Result.build(null, ResultCodeEnum.SECKILL_FINISH);  //卖完了
        }
        //3:商品是否还在抢购时间中  未开始 或是已经结束
        Date startTime = seckillGoods.getStartTime();
        Date endTime = seckillGoods.getEndTime();
        Date curTime = new Date();

        //活动未开始   210
        if (DateUtil.dateCompare(curTime, startTime)) {
            return Result.build(null, ResultCodeEnum.SECKILL_NO_START);
        }
        //活动结束了  214
        if (DateUtil.dateCompare(endTime, curTime)) {
            return Result.build(null, ResultCodeEnum.SECKILL_END);
        }
        // 活动开始了
        //UUID  记录下来  RedisTemplate
        //  200
        String no = MD5.encrypt(userId + "::" + skuId);
        return Result.ok(no);
    }

    //开始秒杀
    @GetMapping("/auth/seckillOrder/{skuId}")
    public Result seckillOrder(@PathVariable Long skuId, String skuIdStr, HttpServletRequest request) {
        //        1、下单码是否正确
        String userId = AuthContextHolder.getUserId(request);
        String encrypt = MD5.encrypt(userId + "::" + skuId);
        if(null == skuIdStr || !skuIdStr.equals(encrypt)){
            return Result.build(null,ResultCodeEnum.SECKILL_ILLEGAL);
        }

        //        2、校验是否有库存（内存）  skuId:1  ==  商品ID:是否有货状态  0 1
        Object state = CacheHelper.get(skuId.toString());
        if(null == state || !state.equals("1")){
            return Result.build(null,ResultCodeEnum.SECKILL_FINISH);
        }
        //发送消息的对象
        UserRecode userRecode = new UserRecode();
        userRecode.setUserId(userId);
        userRecode.setSkuId(skuId);
//        3、发消息
        rabbitService.sendMessage(MQConst.EXCHANGE_DIRECT_SECKILL_USER,
                MQConst.ROUTING_SECKILL_USER,userRecode);

//        4：200
        return Result.ok(); //前端收到200之后开始轮询查询用户状态

    }

    //轮循查询 状态
    @GetMapping("/auth/checkOrder/{skuId}")
    public Result checkOrder(@PathVariable Long skuId,HttpServletRequest request){
        String userId = AuthContextHolder.getUserId(request);
//        1、查询是否购买订单
        Boolean key = redisTemplate.hasKey(RedisConst.SECKILL_ORDERS_USERS + userId);
        if (key) {
            return Result.build(null,ResultCodeEnum.SECKILL_ORDER_SUCCESS); //用户已经抢到资格  应该去下单
        }

        Boolean userKey = redisTemplate.hasKey(RedisConst.SECKILL_USER + userId);
        if (userKey) {
            //        3、查询用户是否具备抢购资格
            Boolean hasKey = redisTemplate.hasKey(RedisConst.SECKILL_ORDERS + userId);
            if (hasKey) {
                return Result.build(null,ResultCodeEnum.SECKILL_SUCCESS);  //抢购成功 我的订单
            }
            return Result.build(null,ResultCodeEnum.SECKILL_FAIL);  //抢购失败
        }
        //        4、用户正在排队
        return Result.build(null, ResultCodeEnum.SECKILL_RUN);  //正在排队中
    }

}
