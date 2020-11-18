package com.atguigu.gmall.item.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.client.ItemFeignClient;
import org.springframework.stereotype.Service;

/**
 * @title: ItemDegradeFeignClient
 * @Author LiuXianKun
 * @Date: 2020/11/18 10:09
 */
@Service
public class ItemDegradeFeignClient implements ItemFeignClient {
    @Override
    public Result getItem(Long skuId) {
        return Result.fail();
    }
}
