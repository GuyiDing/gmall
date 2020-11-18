package com.atguigu.gmall.item.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.item.service.ItemService;
import com.atguigu.gmall.model.product.SkuInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
    public Result getItem(@PathVariable("skuId") Long skuId) {
        List<SkuInfo> list = itemService.getItem(skuId);
        return Result.ok(list);
    }

}
