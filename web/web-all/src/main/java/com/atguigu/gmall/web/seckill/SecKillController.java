//package com.atguigu.gmall.web.seckill;
//
////import com.atguigu.gmall.activity.client.SeckillFeignClient;
//import com.atguigu.gmall.common.result.ResultCodeEnum;
//import com.atguigu.gmall.common.util.AuthContextHolder;
//import com.atguigu.gmall.common.util.MD5;
//import com.atguigu.gmall.model.activity.SeckillGoods;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.ui.Model;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.PathVariable;
//
//import javax.servlet.http.HttpServletRequest;
//import java.util.List;
//
///**
// * @title: SecKillController
// * @Author LiuXianKun
// * @Date: 2020/12/10 18:23
// */
//@Controller
//public class SecKillController {
//
//
//    @Autowired
//    private SeckillFeignClient seckillFeignClient;
//
//    //路径
//    @GetMapping("/index.html")
//    public String index(Model model) {
//        //秒杀商品集合
//        List<SeckillGoods> seckillGoodsList = seckillFeignClient.getSeckillGoodsList();
//        model.addAttribute("list", seckillGoodsList);
//        return "seckill/index";
//    }
//
//    //商品详情页面
//    @GetMapping("/seckill/{skuId}.html")
//    public String seckillItem(@PathVariable Long skuId, Model model) {
//        SeckillGoods seckillGoods = seckillFeignClient.getSeckillGoodsBySkuId(skuId);
//        model.addAttribute("item", seckillGoods);
//        return "seckill/item";
//    }
//
//    //秒杀排队页面
//    @GetMapping("/seckill/queue.html")
//    public String queue(Long skuId, String skuIdStr, HttpServletRequest request) {
//        String userId = AuthContextHolder.getUserId(request);
//        //1:校验  抢购号是否正确
//        String encrypt = MD5.encrypt(userId + "::" + skuId);
//        if (null != skuIdStr && skuIdStr.equals(encrypt)) {
//            //2:正确  进入秒杀排队页面
//            request.setAttribute("skuId", skuId);
//            request.setAttribute("skuIdStr", skuIdStr);
//            return "seckill/queue";
//        } else {
//            //3:错误 进入错误页面
//            return "redirect:http://activity.gmall.com/seckill/fail.html";
//        }
//    }
//
//    //转发错误页面
//    @GetMapping("/seckill/fail.html")
//    public String fail(Model model) {
//        model.addAttribute("message", ResultCodeEnum.SECKILL_ILLEGAL.getMessage());
//        return "seckill/fail";
//    }
//}
