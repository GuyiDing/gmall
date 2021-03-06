package com.atguigu.gmall.product.api;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManagerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

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
    public SkuInfo getSkuInfo(@PathVariable("skuId") Long skuId) {
        return managerService.getSkuInfo(skuId);
    }

    //获取分类视图
    @GetMapping("inner/getCategoryView/{category3Id}")
    public BaseCategoryView getCategoryView(@PathVariable("category3Id") Long category3Id) {
        return managerService.getCategoryViewByCategory3Id(category3Id);
    }

    @GetMapping("inner/getPrice/{skuId}")
    public BigDecimal getPrice(@PathVariable("skuId") Long skuId) {
        return managerService.getPrice(skuId);
    }


    @GetMapping("selectSpuSaleAttrListCheckBySkuId/{skuId}/{spuId}")
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySkuId(@PathVariable("skuId") Long skuId,
                                                               @PathVariable("spuId") Long spuId) {
        return managerService.selectSpuSaleAttrListCheckBySkuId(skuId,spuId);
    }


    @GetMapping("inner/getSkuValueIdsMap/{spuId}")
    public Map<String,String> getSkuValueIdsMap(@PathVariable("spuId") Long spuId){
        return managerService.getSkuValueIdsMap(spuId);
    }


    @GetMapping("inner/getBaseCategoryList")
    public List<BaseCategoryView> getBaseCategoryList() {

        return managerService.getBaseCategoryList();
    }

    //通过品牌Id 集合来查询数据
    @GetMapping("inner/getTrademark/{tmId}")
    public BaseTrademark getTrademark(@PathVariable("tmId") Long tmId) {
        return managerService.getTrademark(tmId);
    }

    //通过skuId 集合来查询数据
    @GetMapping("inner/getAttrList/{skuId}")
    public List<BaseAttrInfo> getAttrList(@PathVariable("skuId") Long skuId) {
        return managerService.getAttrList(skuId);
    }



}
