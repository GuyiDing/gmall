package com.atguigu.gmall.product.mapper;

import com.atguigu.gmall.model.product.SpuSaleAttr;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SpuSaleAttrMapper extends BaseMapper<SpuSaleAttr> {
    List<SpuSaleAttr> spuSaleAttrList(@Param("spuId") Long spuId);

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySkuId(@Param("skuId") Long skuId, @Param("spuId") Long spuId);
}
