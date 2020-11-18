package com.atguigu.gmall.product.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.service.ManagerService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @title: ManagerController
 * @Author LiuXianKun
 * @Date: 2020/11/14 18:39
 */


@Api(tags = "商品基础属性接口")
//@CrossOrigin
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@RestController
@RequestMapping("/admin/product")
public class ManagerController {

    @Autowired
    private ManagerService managerService;

    @GetMapping("/getCategory1")
    public Result getCategory1() {
        List<BaseCategory1> list = managerService.getCategory1();
        return Result.ok(list);
    }

    @GetMapping("getCategory2/{id}")
    public Result getCategory2(@PathVariable Long id) {
        List<BaseCategory2> list = managerService.getCategory2(id);
        return Result.ok(list);
    }

    @GetMapping("getCategory3/{id}")
    public Result getCategory3(@PathVariable Long id) {
        List<BaseCategory3> list = managerService.getCategory3(id);
        return Result.ok(list);
    }

    @GetMapping("attrInfoList/{category1Id}/{category2Id}/{category3Id}")
    public Result getAttrInfoList(@PathVariable("category1Id") Long category1Id,
                                  @PathVariable("category2Id") Long category2Id,
                                  @PathVariable("category3Id") Long category3Id) {
        List<BaseAttrInfo> list = managerService.getAttrInfoList(category1Id, category2Id, category3Id);
        return Result.ok(list);
    }

    @PostMapping("saveAttrInfo")
    public Result saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo) {
        managerService.saveAttrInfo(baseAttrInfo);
        return Result.ok();
    }

    @GetMapping("getAttrValueList/{id}")
    public Result getAttrValueList(@PathVariable Long id) {
        List<BaseAttrValue> list = managerService.getAttrValueList(id);

        return Result.ok(list);
    }

    @GetMapping("baseTrademark/{page}/{limit}")
    public Result baseTrademark(@PathVariable("page") Integer page,
                                @PathVariable("limit") Integer limit) {

        IPage<BaseTrademark> iPage = managerService.baseTrademark(page, limit);
        return Result.ok(iPage);
    }

    @GetMapping("baseTrademark/getTrademarkList")
    public Result getTrademarkList() {
        List<BaseTrademark> list = managerService.getTrademarkList();
        return Result.ok(list);
    }

    @GetMapping("baseSaleAttrList")
    public Result getBaseSaleAttrList() {
        List<BaseSaleAttr> baseSaleAttrList = managerService.getBaseSaleAttrList();

        return Result.ok(baseSaleAttrList);
    }

    @PostMapping("saveSpuInfo")
    public Result saveSpuInfo(@RequestBody SpuInfo spuInfo) {
        managerService.saveSpuInfo(spuInfo);

        return Result.ok();
    }

    @GetMapping("{page}/{limit}")
    public Result get(@PathVariable("page") Integer page,
                      @PathVariable("limit") Integer limit,
                      SpuInfo spuInfo) {
        Page<SpuInfo> spuInfoPage = new Page<>(page, limit);
        IPage<SpuInfo> iPage = managerService.getSpu(spuInfoPage, spuInfo);

        return Result.ok(iPage);
    }

    @GetMapping("spuImageList/{spuId}")
    public Result spuImageList(@PathVariable("spuId") Long spuId) {
        List<SpuImage> list = managerService.spuImageList(spuId);
        return Result.ok(list);
    }

    @GetMapping("spuSaleAttrList/{spuId}")
    public Result spuSaleAttrList(@PathVariable("spuId") Long spuId) {
        List<SpuSaleAttr> list = managerService.spuSaleAttrList(spuId);
        return Result.ok(list);
    }

    @PostMapping("saveSkuInfo")
    public Result saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        managerService.saveSkuInfo(skuInfo);
        return Result.ok();
    }

    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId) {
        managerService.onSale(skuId);
        return Result.ok();
    }

    @GetMapping("cancelSale/{skuId}")
    public Result cancelSale(@PathVariable("skuId") Long skuId) {
        managerService.cancelSale(skuId);
        return Result.ok();
    }

    @GetMapping("list/{page}/{limit}")
    public Result list(@PathVariable("page") Integer page,
                       @PathVariable("limit") Integer limit) {
        IPage<SkuInfo> list = managerService.list(page, limit);
        return Result.ok(list);
    }

}
