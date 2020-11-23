package com.atguigu.gmall.web.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.common.pool.ThreadPool;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import com.atguigu.gmall.web.item.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @title: ItemService
 * @Author LiuXianKun
 * @Date: 2020/11/17 19:54
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class ItemServiceImpl implements ItemService {

    @Autowired
    private ProductFeignClient productFeignClient;

    @Autowired
    private ThreadPool threadPool;

    @Override
    public Map<String, Object> getItem(Long skuId) {
        Map map = new HashMap<>();
        CompletableFuture<SkuInfo> skuInfoCompletableFuture = CompletableFuture.supplyAsync(() -> {
            SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
            map.put("skuInfo", skuInfo);
            return skuInfo;
        }, threadPool.getThreadPool());

        CompletableFuture<Void> categoryViewCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
            map.put("categoryView", categoryView);
        }, threadPool.getThreadPool());

        CompletableFuture<Void> priceCompletableFuture = CompletableFuture.runAsync(() -> {
            BigDecimal price = productFeignClient.getPrice(skuId);
            map.put("price", price);
        }, threadPool.getThreadPool());

        CompletableFuture<Void> spuSaleAttrListCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            List<SpuSaleAttr> spuSaleAttrList = productFeignClient.selectSpuSaleAttrListCheckBySkuId(skuId, skuInfo.getSpuId());
            map.put("spuSaleAttrList", spuSaleAttrList);
        }, threadPool.getThreadPool());

        CompletableFuture<Void> valuesSkuJsonCompletableFuture = skuInfoCompletableFuture.thenAcceptAsync(skuInfo -> {
            Map<String, String> valuesSkuJson = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
            map.put("valuesSkuJson", JSON.toJSONString(valuesSkuJson));
        }, threadPool.getThreadPool());

        //等待线程都执行完成
        CompletableFuture.allOf(skuInfoCompletableFuture, valuesSkuJsonCompletableFuture, spuSaleAttrListCompletableFuture,
                priceCompletableFuture, categoryViewCompletableFuture
        ).join();
        return map;
    }
}
