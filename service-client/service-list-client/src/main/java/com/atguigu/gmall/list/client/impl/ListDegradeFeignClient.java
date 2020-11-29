package com.atguigu.gmall.list.client.impl;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.stereotype.Service;

/**
 * @title: ListDegradeFeignClient
 * @Author LiuXianKun
 * @Date: 2020/11/24 23:15
 */
@Service
public class ListDegradeFeignClient  implements ListFeignClient {
    @Override
    public Result incrHotScore(Long skuId) {
        return null;
    }

    @Override
    public SearchResponseVo list(SearchParam listParam) {
        return null;
    }

    @Override
    public Result onSale(Long skuId) {
        return null;
    }

    @Override
    public Result cancel(Long skuId) {
        return null;
    }
}
