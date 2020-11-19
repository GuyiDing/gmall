package com.atguigu.gmall.product.service;

import com.atguigu.gmall.model.product.*;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csource.common.MyException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface ManagerService {


    List<BaseCategory1> getCategory1();

    List<BaseCategory2> getCategory2(Long id);

    List<BaseCategory3> getCategory3(Long id);

    List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id);

    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(Long id);

    IPage<BaseTrademark> baseTrademark(Integer page, Integer limit);

    List<BaseTrademark> getTrademarkList();

    String fileUpload(MultipartFile file) throws IOException, MyException;

    List<BaseSaleAttr> getBaseSaleAttrList();

    void saveSpuInfo(SpuInfo spuInfo);

    IPage<SpuInfo> getSpu(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo);

    List<SpuImage> spuImageList(Long spuId);

    List<SpuSaleAttr> spuSaleAttrList(Long spuId);

    void saveSkuInfo(SkuInfo skuInfo);

    void onSale(Long skuId);

    void cancelSale(Long skuId);

    IPage<SkuInfo> list(Integer page, Integer limit);

    SkuInfo getSkuInfo(Long skuId);

    BaseCategoryView getCategoryViewByCategory3Id(Long category3Id);

    BigDecimal getPrice(Long skuId);

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySkuId(Long skuId, Long spuId);

    Map<String,String> getSkuValueIdsMap(Long spuId);
}
