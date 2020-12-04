package com.atguigu.gmall.rabbit.entity;

import lombok.Data;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.stereotype.Component;

/**
 * @title: GmallCorrelationData
 * @Author LiuXianKun
 * @Date: 2020/12/4 16:25
 */

@Data
@Component
public class GmallCorrelationData extends CorrelationData {
    //消息体
    private Object message;
    //交换机
    private String exchange;
    //路由键
    private String routingKey;
    //重发次数
    private int retryCount = 0;
    //是否延迟消息
    private boolean isDelay = false;
    //延迟时长
    private int delayTime = 10;
}
