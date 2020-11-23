package com.atguigu.gmall.product.client;

import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.atguigu.gmall.product.client.impl.ProductFeignClientImpl;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@FeignClient(value = "service-product", fallback = ProductFeignClientImpl.class)
public interface ProductFeignClient {
    @GetMapping("api/product/inner/getCategoryView/{category3Id}")
    BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id);

    /**
     * 根据skuId获取sku信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("api/product/inner/getSkuInfo/{skuId}")
    SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId);

    @GetMapping("api/product/inner/getPrice/{skuId}")
    BigDecimal getPrice(@PathVariable("skuId") Long skuId);

    @GetMapping("api/product/selectSpuSaleAttrListCheckBySkuId/{skuId}/{spuId}")
    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySkuId(@PathVariable("skuId") Long skuId,
                                                        @PathVariable("spuId") Long spuId);

    @GetMapping("api/product/inner/getSkuValueIdsMap/{spuId}")
    Map<String, String> getSkuValueIdsMap(@PathVariable("spuId") Long spuId);

    @GetMapping("api/product/inner/getBaseCategoryList")
    List<BaseCategoryView> getBaseCategoryList();
}
