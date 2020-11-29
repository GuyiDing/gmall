package com.atguigu.gmall.web.index;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.Resource;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @title: IndexController
 * @Author LiuXianKun
 * @Date: 2020/11/23 10:20
 */

@SuppressWarnings("Duplicates")
@Controller
public class IndexController {

    @Resource
    private ProductFeignClient productFeignClient;

    //    @GetMapping({"/","/index"})
//    public String index() {
//        return "index/index";
//    }
    @GetMapping("/")
    public String index(Model model) {
        //获取数据
        List<Map<String, Object>> listMap = getData();
        model.addAttribute("list", listMap);
        return "index/index";
    }

    private List<Map<String, Object>> getData() {
        List<BaseCategoryView> categoryViewList = productFeignClient.getBaseCategoryList();
        List<Map<String, Object>> mapList1 = new ArrayList<>();
        int index = 0;
        Map<Long, List<BaseCategoryView>> collect1 = categoryViewList.stream()
                .collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
        for (Map.Entry<Long, List<BaseCategoryView>> entry1 : collect1.entrySet()) {
            Map<String, Object> resultMap1 = new HashMap<>();
            List<BaseCategoryView> entry1Value = entry1.getValue();
            resultMap1.put("categoryId", entry1.getKey());
            resultMap1.put("categoryName", entry1Value.get(0).getCategory1Name());
            resultMap1.put("index", ++index);


            List<Map<String, Object>> mapList2 = new ArrayList<>();
            Map<Long, List<BaseCategoryView>> collect2 = entry1Value.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
            for (Map.Entry<Long, List<BaseCategoryView>> entry2 : collect2.entrySet()) {
                List<BaseCategoryView> entry2Value = entry2.getValue();
                Map<String, Object> resultMap2 = new HashMap<>();
                resultMap2.put("categoryId", entry2.getKey());
                resultMap2.put("categoryName", entry2Value.get(0).getCategory2Name());
                List<Map<String, Object>> mapList3 = new ArrayList<>();
                for (BaseCategoryView baseCategoryView3 : entry2Value) {
                    Map<String, Object> resultMap3 = new HashMap<>();
                    resultMap3.put("categoryId", baseCategoryView3.getCategory3Id());
                    resultMap3.put("categoryName", baseCategoryView3.getCategory3Name());
                    mapList3.add(resultMap3);
                }

                resultMap2.put("categoryChild", mapList3);
                mapList2.add(resultMap2);

            }
            resultMap1.put("categoryChild", mapList2);
            mapList1.add(resultMap1);
        }

        return mapList1;
    }

//    private List<Map<String, Object>> getData() {
//        List<BaseCategoryView> categoryViewList = productFeignClient.getBaseCategoryList();
//        List<Map<String, Object>> mapList1 = new ArrayList<>();
//
//        Map<Long, List<BaseCategoryView>> collect1 = categoryViewList.stream()
//                .collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
//        AtomicInteger index = new AtomicInteger(0);
//
//
//        //int index = 0;
//        collect1.forEach((category1Id, baseCategoryViews1) -> {
//
//            Map<String, Object> resultMap1 = new HashMap<>();
//            resultMap1.put("index", index.incrementAndGet());
//           // resultMap1.put("index", ++index);
//
//
//            resultMap1.put("categoryId", category1Id);
//            resultMap1.put("categoryName", baseCategoryViews1.get(0).getCategory1Name());
//
//            Map<Long, List<BaseCategoryView>> collect2 = baseCategoryViews1.stream()
//                    .collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
//
//            collect2.forEach((category2Id, baseCategoryViews2) -> {
//                List<Map<String, Object>> mapList2 = new ArrayList<>();
//                Map<String, Object> resultMap2 = new HashMap<>();
//                resultMap2.put("categoryId", category2Id);
//                resultMap2.put("categoryName", baseCategoryViews2.get(0).getCategory1Name());
//                Map<Long, List<BaseCategoryView>> collect3 = baseCategoryViews2.stream()
//                        .collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
//                collect3.forEach((category3Id, baseCategoryViews3) -> {
//                    List<Map<String, Object>> mapList3;
//                    mapList3 = new ArrayList<>();
//                    Map<String, Object> resultMap3 = new HashMap<>();
//                    resultMap3.put("categoryId", category3Id);
//                    resultMap3.put("categoryName", baseCategoryViews3.get(0).getCategory3Name());
//                    mapList3.add(resultMap3);
//                });
//                resultMap2.put("categoryChild", mapList3);
//                mapList2.add(resultMap2);
//            });
//            resultMap1.put("categoryChild", mapList2);
//            mapList1.add(resultMap1);
//        });
//
//        return mapList1;
//    }

    @Autowired
    private TemplateEngine templateEngine;

    @GetMapping("/createHtml")
    @ResponseBody
    public Result createHtml() {
        Context context = new Context();
        List<Map<String, Object>> data = getData();
        context.setVariable("list", data);
        Writer writer = null;
        try {
            writer = new PrintWriter(new File("D:\\temp\\index.html"), "UTF-8");
            templateEngine.process("index/index", context, writer);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return Result.ok();
    }


}
