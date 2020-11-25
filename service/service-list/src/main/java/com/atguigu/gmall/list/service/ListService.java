package com.atguigu.gmall.list.service;


import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;

public interface ListService {
    void cancel(Long skuId);

    void onSale(Long skuId);

    void hotScore(Long skuId);

    SearchResponseVo list(SearchParam searchParam);
}
