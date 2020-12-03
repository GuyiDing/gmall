package com.example.canatest.config;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.atguigu.gmall.model.cart.CartInfo;
import com.google.protobuf.Descriptors;
import com.xpand.starter.canal.annotation.CanalEventListener;
import com.xpand.starter.canal.annotation.ListenPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @author chen.qian
 * @date 2018/3/19
 */
@CanalEventListener
public class MyEventListener {

    //表示： Mysql发生 插入时  将插入的数据监听过来
/*
    @InsertListenPoint
    public void onEvent(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {
        rowData.getAfterColumnsList().forEach((c) -> System.err.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    }

    @UpdateListenPoint
    public void onEvent1(CanalEntry.RowData rowData) {
        System.err.println("UpdateListenPoint");
        rowData.getAfterColumnsList().forEach((c) -> System.err.println("By--Annotation: " + c.getName() + " ::   " + c.getValue()));
    }

    @DeleteListenPoint
    public void onEvent3(CanalEntry.EventType eventType) {
        System.err.println("DeleteListenPoint");
    }
*/


    /**
     * Mysql有变化 监听数据过来
     *
     * @param eventType
     * @param rowData   destination ： canal的服务端的名称
     * schema ： 数据库（Mysql）
     * table ： 表 （Mysql）
     * eventType ： 发生什么样的事件 将数据监听过来
     */

    @Autowired
    private RedisTemplate redisTemplate;

    @ListenPoint(destination = "example",
            schema = {"gmall_order"},
            table = {"cart_info"},
            eventType = {CanalEntry.EventType.UPDATE,
                    CanalEntry.EventType.INSERT,
                    CanalEntry.EventType.QUERY
            }
    )
    public void onEvent4(CanalEntry.EventType eventType, CanalEntry.RowData rowData) {

        rowData.getAfterColumnsList().forEach((c) ->
                System.err.println("key: " + c.getName() +
                        " :value:   " + c.getValue()));

        CartInfo cartInfo = new CartInfo();

        Map<Descriptors.FieldDescriptor, Object> allFields = rowData.getAllFields();

        for (CanalEntry.Column column : rowData.getAfterColumnsList()) {
            switch (column.getName()) {
                case "user_id":
                    cartInfo.setUserId(column.getValue());
                    break;
                case "sku_id":
                    cartInfo.setSkuId(Long.parseLong(column.getValue()));
                    break;
                case "cart_price":
                    cartInfo.setCartPrice(new BigDecimal(column.getValue()));
                    break;
                case "sku_num":
                    cartInfo.setSkuNum(Integer.parseInt(column.getValue()));
                    break;
                case "img_url":
                    cartInfo.setImgUrl(column.getValue());
                    break;
                case "sku_name":
                    cartInfo.setSkuName(column.getValue());
                    break;
                case "is_checked":
                    cartInfo.setIsChecked(Integer.parseInt(column.getValue()));
                    break;
            }
        }
        redisTemplate.opsForHash().put(cartInfo.getUserId(), cartInfo.getSkuId().toString(), cartInfo);


    }
}
