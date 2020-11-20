package com.atguigu.gmall.web.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.web.item.service.ItemService;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Override
    public Map<String,Object> getItem(Long skuId) {
        Map map = new HashMap<>();
        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        map.put("skuInfo", skuInfo);
        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        map.put("categoryView", categoryView);
        BigDecimal price = productFeignClient.getPrice(skuId);
        map.put("price", price);
        List<SpuSaleAttr> spuSaleAttrList = productFeignClient.selectSpuSaleAttrListCheckBySkuId(skuId, skuInfo.getSpuId());
        map.put("spuSaleAttrList", spuSaleAttrList);
        Map<String, String> valuesSkuJson = productFeignClient.getSkuValueIdsMap(skuInfo.getSpuId());
        map.put("valuesSkuJson", JSON.toJSONString(valuesSkuJson));
        return map;
    }
}
