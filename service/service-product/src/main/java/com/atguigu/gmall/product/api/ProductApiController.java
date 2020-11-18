package com.atguigu.gmall.product.api;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @title: ProductApiController
 * @Author LiuXianKun
 * @Date: 2020/11/17 20:37
 */
@RestController
@RequestMapping("api/product")
public class ProductApiController {
    @Autowired
    private ManagerService managerService;

    /**
     * 根据skuId获取sku信息
     *
     * @param skuId
     * @return
     */
    @GetMapping("inner/getSkuInfo/{skuId}")
    public Result getSkuInfo(@PathVariable("skuId") Long skuId) {
        return Result.ok(managerService.getSkuInfo(skuId));
    }

    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id")Long category3Id){
        return managerService.getCategoryViewByCategory3Id(category3Id);
    }



}
