package com.atguigu.gmall.web.item.client.impl;

import com.atguigu.gmall.web.item.client.ItemFeignClient;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * @title: ItemDegradeFeignClient
 * @Author LiuXianKun
 * @Date: 2020/11/18 10:09
 */
@Service
public class ItemDegradeFeignClient implements ItemFeignClient {

    @Override
    public Map<String, Object> getItem(Long skuId) {
        return null;
    }
}
