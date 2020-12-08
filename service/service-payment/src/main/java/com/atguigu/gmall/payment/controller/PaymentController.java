package com.atguigu.gmall.payment.controller;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.payment.service.AliPayService;
import com.atguigu.gmall.payment.service.PaymentInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

/**
 * @title: PaymentController
 * @Author LiuXianKun
 * @Date: 2020/12/6 15:20
 */
@Controller
@RequestMapping("api/payment/alipay")
public class PaymentController {

    @Resource
    private PaymentInfoService paymentInfoService;
    @Autowired
    private AliPayService aliPayService;

    @ResponseBody
    @GetMapping("/submit/{orderId}")
    public String alipaySubmit(@PathVariable Long orderId) {
        return aliPayService.alipaySubmit(orderId);
    }

    @GetMapping("/callback/return")  //支付宝响应之后的回调地址
    public String callBackReturn() {
        return "redirect:" + AlipayConfig.return_order_url;
    }


    @ResponseBody
    @PostMapping("/callback/notify")
    public String callBackNotify(@RequestParam Map<String, String> paramsMap) {
        try {
            //认证  确实是由支付宝服务发来的
            boolean signVerified = AlipaySignature.rsaCheckV1(paramsMap,
                    AlipayConfig.alipay_public_key, AlipayConfig.charset,
                    AlipayConfig.sign_type); //调用SDK验证签名
            if (signVerified) {
                // TODO 验签成功后，按照支付结果异步通知中的描述，对支付结果中的业务内容进行二次校验，校验成功后在response中返回success并继续商户自身业务处理，校验失败返回failure
                System.out.println("由支付宝服务发来的消息接收成功");
                System.out.println(paramsMap);

       /*         {
                    gmt_create=2020-12-07 11:29:56,
                        charset=utf-8,
                        gmt_payment=2020-12-07 11:30:06,
                        notify_time=2020-12-07 11:30:07,
                        subject=华为 HUAWEI Mate 30 5G 麒麟990 4000万超感光徕卡影像双超级快充8GB+128GB亮黑色5G全网通游戏手机 华为 HUAWEI Mate 30 5G 麒麟990 4000万超感,
                        buyer_id=2088622955148930,
                        invoice_amount=35993.00, version=1.0,
                        notify_id=2020120700222113006048930509798623,
                        fund_bill_list=[{"amount":"35993.00","fundChannel":"ALIPAYACCOUNT"}],
                    notify_type=trade_status_sync,
                            out_trade_no=ATGUIGU1607311791509419,
                            total_amount=35993.00,
                            trade_status=TRADE_SUCCESS,
                            trade_no=2020120722001448930501179305,
                            auth_app_id=2016102100732915,
                            receipt_amount=35993.00, point_amount=0.00,
                            app_id=2016102100732915, buyer_pay_amount=35993.00,
                            seller_id=2088102180533564}*/
                //判断交易是否成功
                if ("TRADE_SUCCESS".equals(paramsMap.get("trade_status"))) {
                    //成功了
                    //更新支付表相关状态及数据
                    paymentInfoService.updateByOutTradeNo(paramsMap);
                } else {
                    //
                }
                //业务相关
                return "success";
            } else {
                // TODO 验签失败则记录异常日志，并在response中返回failure.
                System.out.println("由支付宝服务发来的消息接收失败");
                //记录日志
                return "failure";
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        System.out.println("由支付宝服务发来的消息接收失败");
        //记录日志
        return "failure";
    }


    //关闭二维码
    @GetMapping("/closePay/{outTradeNo}")
    public Result closePay(@PathVariable String outTradeNo){
        aliPayService.closePay(outTradeNo);

        return Result.ok();
    }
}



