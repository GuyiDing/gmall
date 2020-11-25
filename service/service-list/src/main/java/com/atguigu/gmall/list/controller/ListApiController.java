package com.atguigu.gmall.list.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.web.bind.annotation.*;

/**
 * @title: ListApiController
 * @Author LiuXianKun
 * @Date: 2020/11/23 20:39
 */
@RestController
@RequestMapping("api/list")
public class ListApiController {
    @Autowired
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Autowired
    private ListService listService;


    @GetMapping("/inner/createIndex")
    public void createIndex() {
        elasticsearchRestTemplate.createIndex(Goods.class);
        elasticsearchRestTemplate.putMapping(Goods.class);
    }


    @GetMapping("onSale/{skuId}")
    public Result onSale(@PathVariable("skuId") Long skuId) {
        listService.onSale(skuId);
        return Result.ok();
    }


    @GetMapping("cancel/{skuId}")
    public Result cancel(@PathVariable("skuId") Long skuId) {
        listService.cancel(skuId);
        return Result.ok();
    }

    //商品热度更新
    @GetMapping("/inner/hotScore/{skuId}")
    public Result hotScore(@PathVariable(name = "skuId") Long skuId){

        listService.hotScore(skuId);
        return Result.ok();
    }

    @PostMapping
    public SearchResponseVo list(@RequestBody SearchParam searchParam){
        return listService.list(searchParam);
    }
}
