package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManagerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @title: ManagerServiceImpl
 * @Author LiuXianKun
 * @Date: 2020/11/14 18:48
 */
@SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
@Service
public class ManagerServiceImpl implements ManagerService {
    @Autowired
    private BaseCategory1Mapper baseCategory1Mapper;
    @Autowired
    private BaseCategory2Mapper baseCategory2Mapper;
    @Autowired
    private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;


    @Override
    public List<BaseCategory1> getCategory1() {
        return baseCategory1Mapper.selectList(null);
    }

    @Override
    public List<BaseCategory2> getCategory2(Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("category1_id", id);
        return baseCategory2Mapper.selectList(queryWrapper);
    }

    @Override
    public List<BaseCategory3> getCategory3(Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("category2_id", id);
        return baseCategory3Mapper.selectList(queryWrapper);
    }

    @Override
    public List<BaseAttrInfo> getAttrInfoList(Long category1Id, Long category2Id, Long category3Id) {
        List<BaseAttrInfo> list = baseAttrInfoMapper.getAttrInfoList(category1Id, category2Id, category3Id);

        return list;
    }

    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        if (baseAttrInfo.getId() == null) {
            // 修改数据
            baseAttrInfoMapper.updateById(baseAttrInfo);
        } else {
            //新增
            baseAttrInfoMapper.insert(baseAttrInfo);
        }

        //删除
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("attr_id", baseAttrInfo.getId());
        baseAttrValueMapper.delete(queryWrapper);
//
//
//        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
//        if (attrValueList.size() > 0 && attrValueList != null) {
//            for (BaseAttrValue attrValue : attrValueList) {
//                attrValue.setAttrId(baseAttrInfo.getId());
//                baseAttrValueMapper.insert(attrValue);
//            }
//        }


    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("attr_id", id);
        return baseAttrValueMapper.selectList(queryWrapper);

    }
}
