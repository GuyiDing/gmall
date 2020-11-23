package com.atguigu.gmall.web.item;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.web.item.client.ItemFeignClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @title: controller
 * @Author LiuXianKun
 * @Date: 2020/11/18 21:16
 */

@Controller
@RequestMapping
public class controller {
    @Resource
    private ItemFeignClient itemFeignClient;

    @RequestMapping("/{skuId}.html")
    public String getItem(@PathVariable("skuId") Long skuId, Model model) {
        Map<String, Object> item = itemFeignClient.getItem(skuId);
        model.addAllAttributes(item);
        return "item/index";
    }
}
