package com.atguigu.gmall.web.item.controller;

import com.atguigu.gmall.web.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @title: ItemApiController
 * @Author LiuXianKun
 * @Date: 2020/11/17 19:46
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RestController
@RequestMapping("api/item")
public class ItemApiController {
    @Autowired
    private ItemService itemService;

    //获取sku详情item
    @GetMapping("{skuId}")
    public Map<String,Object> getItem(@PathVariable("skuId") Long skuId) {

        return itemService.getItem(skuId);
    }

}
