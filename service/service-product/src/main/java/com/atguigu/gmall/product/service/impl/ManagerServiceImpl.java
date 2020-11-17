package com.atguigu.gmall.product.service.impl;

import com.atguigu.gmall.model.product.*;
import com.atguigu.gmall.product.mapper.*;
import com.atguigu.gmall.product.service.ManagerService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient1;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
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
    @Autowired
    private BaseTrademarkMapper baseTrademarkMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;

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
        if (baseAttrInfo.getId() != null) {
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


        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
        if (attrValueList.size() > 0 && attrValueList != null) {
            attrValueList.forEach(baseAttrValue -> {
                baseAttrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insert(baseAttrValue);
            });
        }


    }

    @Override
    public List<BaseAttrValue> getAttrValueList(Long id) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("attr_id", id);
        return baseAttrValueMapper.selectList(queryWrapper);

    }

    @Override
    public IPage<BaseTrademark> baseTrademark(Integer page, Integer limit) {

        return baseTrademarkMapper.selectPage(new Page<>(page, limit), null);

    }

    @Override
    public List<BaseTrademark> getTrademarkList() {

        return baseTrademarkMapper.selectList(null);
    }

    @Value("${fileServer.url}")
    private String fileUrl;

    @Override
    public String fileUpload(MultipartFile file) throws IOException, MyException {
        System.out.println("fileUrl = " + fileUrl);

        String path = ClassUtils.getDefaultClassLoader().getResource("tracker.conf").getPath();
        ClientGlobal.init(path);
        TrackerClient trackerClient = new TrackerClient();

        TrackerServer connection = trackerClient.getConnection();

        //3:连接存储节点Storage

        StorageClient1 storageClient1 = new StorageClient1(connection, null);
        String originalFilename = file.getOriginalFilename();
        System.out.println("originalFilename = " + originalFilename);
        // 获取文件后缀名
        String s = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
        String imgPath = storageClient1.upload_file1(file.getBytes(), s, null);
        System.out.println("imgPath = " + imgPath);
        imgPath = fileUrl + imgPath;
        return imgPath;
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectList(null);
    }

    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {
        spuInfoMapper.insert(spuInfo);
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList.size() > 0 && spuImageList != null) {
            spuImageList.forEach(spuImage -> {
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insert(spuImage);
            });
        }
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList.size() > 0 && spuSaleAttrList != null) {
            spuSaleAttrList.forEach(spuSaleAttr -> {
                spuSaleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(spuSaleAttr);
                List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();
                spuSaleAttrValueList.forEach(spuSaleAttrValue -> {
                    spuSaleAttrValue.setSpuId(spuInfo.getId());
                    spuSaleAttrValue.setSaleAttrName(spuSaleAttr.getSaleAttrName());
                    spuSaleAttrValueMapper.insert(spuSaleAttrValue);
                });
            });
        }


    }

    @Override
    public IPage<SpuInfo> getSpu(Page<SpuInfo> spuInfoPage, SpuInfo spuInfo) {
        QueryWrapper querywrapper = new QueryWrapper();
        querywrapper.eq("category3_id", spuInfo.getCategory3Id());

        return spuInfoMapper.selectPage(spuInfoPage, querywrapper);
    }

    @Override
    public List<SpuImage> spuImageList(Long spuId) {
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("spu_id", spuId);
        return spuImageMapper.selectList(queryWrapper);
    }

    @Override
    public List<SpuSaleAttr> spuSaleAttrList(Long spuId) {

        return spuSaleAttrMapper.spuSaleAttrList(spuId);
    }


    @Autowired
    private SkuInfoMapper skuInfoMapper;

    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Transactional
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {
        skuInfoMapper.insert(skuInfo);
        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        skuImageList.forEach(skuImage -> {
            skuImage.setSkuId(skuInfo.getId());
            skuImageMapper.insert(skuImage);
        });

        skuInfo.getSkuAttrValueList().forEach(skuAttrValue -> {
            skuAttrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insert(skuAttrValue);
        });

        skuInfo.getSkuSaleAttrValueList().forEach(skuSaleAttrValue -> {
            skuSaleAttrValue.setSpuId(skuInfo.getSpuId());
            skuSaleAttrValue.setSkuId(skuInfo.getId());
            skuSaleAttrValueMapper.insert(skuSaleAttrValue);
        });

    }


}
