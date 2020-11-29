package com.atguigu.gmall.web.list;

import com.atguigu.gmall.list.client.ListFeignClient;
import com.atguigu.gmall.model.list.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @title: controller
 * @Author LiuXianKun
 * @Date: 2020/11/26 15:44
 */
@RequestMapping
@Controller
public class ListController {

    @Resource
    private ListFeignClient listFeignClient;

    @GetMapping("list.html")
    public String search(SearchParam searchParam, Model model) {
        SearchResponseVo list = listFeignClient.list(searchParam);
        //回显入参信息
        model.addAttribute("searchParam", searchParam);
        List<SearchResponseTmVo> trademarkList = list.getTrademarkList();
        model.addAttribute("trademarkList", trademarkList);
        List<SearchResponseAttrVo> attrsList = list.getAttrsList();
        model.addAttribute("attrsList", attrsList);
        List<Goods> goodsList = list.getGoodsList();
        model.addAttribute("goodsList", goodsList);
        //5:分页
        Integer pageNo = list.getPageNo();
        Long totalPages = list.getTotalPages();
        model.addAttribute("pageNo", pageNo);
        model.addAttribute("totalPages", totalPages);

        //6.回显品牌选中项
        String trademark = searchParam.getTrademark();
        if (trademark != null) { //tmId:tmName
            String[] split = trademark.split(":");
            model.addAttribute("trademarkParam", "品牌" + split[1]);
        } else {
            model.addAttribute("trademarkParam", null);
        }

        //7.回显平台属性
        List<Map> propsParamsList = buildPropsParamsList(searchParam);
        model.addAttribute("propsParamsList", propsParamsList);
        //8.URL 入参集合
        String urlParam = buildUrlParam(searchParam);
        model.addAttribute("urlParam", urlParam);
        //9.平台属性集合
        Map orderMap = buildOrderMap(searchParam);
        model.addAttribute("orderMap", orderMap);

        return "list/index";
    }

    private Map buildOrderMap(SearchParam searchParam) {
        String order = searchParam.getOrder();//1:desc 1:asc  2:desc 2:asc
        HashMap<Object, Object> map = new HashMap<>();
        if (!StringUtils.isEmpty(order)) {

            String[] s = order.split(":");
            map.put("type", s[0]);
            map.put("sort", s[1]);
        } else {
            map.put("type", "1");
            map.put("sort", "DESC");
        }

        return map;
    }

    private String buildUrlParam(SearchParam searchParam) {
        //使用字符串拼接  StringBuilder 不安全的效率高 StringBuffer 线程安全的 效率低
        StringBuilder sb = new StringBuilder();
        //http://list.gmall.com/list.html
        // ?keyword=%E6%89%8B%E6%9C%BA&trademark=1:%E8%8B%B9%E6%9E%9C
        // &props=1:4G:bb&props=2:骁龙439:CPU型号
        //URL
        String keyword = searchParam.getKeyword();
        if (!StringUtils.isEmpty(keyword)) {
            sb.append("keyword=").append(keyword);
        }
        if (!StringUtils.isEmpty(searchParam.getTrademark())) {
            if (sb.length() > 0) {
                sb.append("&trademark=").append(searchParam.getTrademark());
            }
            sb.append("trademark=").append(searchParam.getTrademark());
        }
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                if (sb.length() > 0) {
                    sb.append("&props=").append(prop);
                } else {
                    sb.append("props=").append(prop);
                }
            }
        }
        return "/list.html?" + sb.toString();
    }

    private List<Map> buildPropsParamsList(SearchParam searchParam) {
        List<Map> propsParamsList = new ArrayList<>();
        String[] props = searchParam.getProps();
        if (props != null && props.length > 0) {
            for (String prop : props) {
                HashMap<Object, Object> map = new HashMap<>();
                String[] s = prop.split(":");
                map.put("attrId", s[0]);
                map.put("attrValue", s[1]);
                map.put("attrName", s[2]);
                propsParamsList.add(map);
            }
        }

        return propsParamsList;
    }

}
