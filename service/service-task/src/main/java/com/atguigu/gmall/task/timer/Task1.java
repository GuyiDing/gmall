package com.atguigu.gmall.task.timer;

import com.atguigu.gmall.rabbit.constants.MQConst;
import com.atguigu.gmall.rabbit.service.RabbitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @title: Task1
 * @Author LiuXianKun
 * @Date: 2020/12/8 20:59
 */

@Component
@EnableScheduling
public class Task1 {

    @Autowired
    private RabbitService rabbitService;

    @Scheduled(cron = "0/5 * * * * ?")
    public void task() {
        System.out.println("\"发出任务，提醒该干活了\" = " + "发出任务，提醒该干活了");

        rabbitService.sendMessage(MQConst.EXCHANGE_DIRECT_TASK,MQConst.ROUTING_TASK_1,"干活啦，5秒提醒你一次");
    }

}
