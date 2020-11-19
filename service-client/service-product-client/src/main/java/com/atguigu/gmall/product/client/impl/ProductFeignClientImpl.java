package com.atguigu.gmall.product.client.impl;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @title: ProductFeignClientImpl
 * @Author LiuXianKun
 * @Date: 2020/11/17 19:41
 */
@Service
public class ProductFeignClientImpl implements ProductFeignClient {

    @Override
    public BaseCategoryView getCategoryView(Long category3Id) {
        return null;
    }

    @Override
    public SkuInfo getSkuInfo(Long skuId) {
        return null;
    }

    @Override
    public BigDecimal getPrice(Long skuId) {
        return null;
    }

    @Override
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySkuId(Long skuId, Long spuId) {
        return null;
    }

    @Override
    public Map<String, String> getSkuValueIdsMap(Long spuId) {
        return null;
    }
}
