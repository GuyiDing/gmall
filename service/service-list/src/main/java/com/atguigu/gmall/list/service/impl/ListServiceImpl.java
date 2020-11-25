package com.atguigu.gmall.list.service.impl;

import com.atguigu.gmall.list.dao.GoodsDao;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.Goods;
import com.atguigu.gmall.model.list.SearchAttr;
import com.atguigu.gmall.model.list.SearchParam;
import com.atguigu.gmall.model.list.SearchResponseVo;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @title: ListServiceImpl
 * @Author LiuXianKun
 * @Date: 2020/11/23 20:56
 */
@Service
public class ListServiceImpl implements ListService {


    @Autowired
    private GoodsDao goodsDao;

    @Resource
    private ProductFeignClient productFeignClient;

    @Override
    public void cancel(Long skuId) {

    }

    @Override
    public void onSale(Long skuId) {
        Goods goods = new Goods();

        SkuInfo skuInfo = productFeignClient.getSkuInfo(skuId);
        goods.setTitle(skuInfo.getSkuName());
        goods.setCreateTime(new Date());
        goods.setPrice(skuInfo.getPrice().doubleValue());
        goods.setDefaultImg(skuInfo.getSkuDefaultImg());


        BaseTrademark trademark = productFeignClient.getTrademark(skuInfo.getTmId());
        goods.setTmId(trademark.getId());
        goods.setTmName(trademark.getTmName());
        goods.setTmLogoUrl(trademark.getLogoUrl());

        BaseCategoryView categoryView = productFeignClient.getCategoryView(skuInfo.getCategory3Id());
        goods.setCategory1Id(categoryView.getCategory1Id());
        goods.setCategory2Id(categoryView.getCategory2Id());
        goods.setCategory3Id(categoryView.getCategory3Id());
        goods.setCategory1Name(categoryView.getCategory1Name());
        goods.setCategory2Name(categoryView.getCategory2Name());
        goods.setCategory3Name(categoryView.getCategory3Name());


        List<BaseAttrInfo> attrList = productFeignClient.getAttrList(skuInfo.getId());
        List<SearchAttr> searchAttrs = attrList.stream().map(baseAttrInfo -> {
            SearchAttr searchAttr = new SearchAttr();
            //平台属性ID
            searchAttr.setAttrId(baseAttrInfo.getId());
            //平台属性名称
            searchAttr.setAttrName(baseAttrInfo.getAttrName());
            //平台属性值名称
            searchAttr.setAttrValue(baseAttrInfo.getAttrValueList().get(0).getValueName());
            return searchAttr;
        }).collect(Collectors.toList());
        goods.setAttrs(searchAttrs);
        goodsDao.save(goods);
    }
    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public void hotScore(Long skuId) {
        String hotScore = "hotScore";
        Double score = redisTemplate.opsForZSet().incrementScore(hotScore, skuId, 1);
        if (score % 10 == 0) {
            Optional<Goods> goodsOptional = goodsDao.findById(skuId);
            Goods goods = goodsOptional.get();
            goods.setHotScore(Math.round(score));
            goodsDao.save(goods);
        }

    }

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    @Override
    public SearchResponseVo list(SearchParam searchParam) {

        //链接ES索引库
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.indices("goods"); //建立索引库
        //构建条件对象
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchQuery("title",searchParam.getKeyword()));

        //构建分页条件
        int pageNo  = 1;
        int pageSize = 5; //totalPages = (pageTotals + pageSize -1 ) /pageSize
        searchSourceBuilder.from((pageNo - 1) * pageSize);
        searchSourceBuilder.size(pageSize);

        //按热度排序
        searchSourceBuilder.sort("hotScore"); //默认降序 SortOrder.DESC
        //高亮显示
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title").preTags("<font style='color:red'>").postTags("</font>");
        searchRequest.source(searchSourceBuilder);
        //参数1：搜索请求对象
        //参数2：搜索选项 默认
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);//返回搜索结果
            SearchHits hits = search.getHits();
            //总条数
            System.out.println("总条数：" +  hits.totalHits);
            //结果集
            hits.forEach(documentFields -> {
                System.out.println("documentFields = " + documentFields);
                Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
                HighlightField highlightField = highlightFields.get("title");
                System.out.println(highlightField.fragments()[0].toString());
            });

        } catch (IOException e) {
            e.printStackTrace();
        }


        return null;
    }

}
