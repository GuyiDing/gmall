package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;

import java.util.List;

public interface ManagerService {


    List<BaseCategory1> getCategory1();

    List<BaseCategory2> getCategory2(Long id);

    List<BaseCategory3> getCategory3(Long id);

    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(Long id);
}
