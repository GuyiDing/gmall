package com.atguigu.gmall.item.service;


import com.atguigu.gmall.model.product.SkuInfo;

import java.util.List;

public interface ItemService {
    List<SkuInfo> getItem(Long skuId);
}
