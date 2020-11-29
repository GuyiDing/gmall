package com.atguigu.gmall.list.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.gmall.list.dao.GoodsDao;
import com.atguigu.gmall.list.service.ListService;
import com.atguigu.gmall.model.list.*;
import com.atguigu.gmall.model.product.BaseAttrInfo;
import com.atguigu.gmall.model.product.BaseCategoryView;
import com.atguigu.gmall.model.product.BaseTrademark;
import com.atguigu.gmall.model.product.SkuInfo;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.nested.ParsedNested;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedLongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.*;
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
        goods.setId(skuInfo.getId());

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
        SearchSourceBuilder searchSourceBuilder = buildSourceBuilder(searchParam);
        searchRequest.source(searchSourceBuilder);
        SearchResponseVo vo = null;
        //搜索
        try {
            SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            //解析结果
            vo = buildSearchResponse(search);
            vo.setPageNo(searchParam.getPageNo());
            vo.setPageSize(searchParam.getPageSize());
            vo.setTotalPages((vo.getTotal() + vo.getPageSize() - 1) / vo.getPageSize());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return vo;
    }

    private SearchResponseVo buildSearchResponse(SearchResponse search) {
        SearchResponseVo vo = new SearchResponseVo();
        Map<String, Aggregation> map = search.getAggregations().asMap();
        ParsedLongTerms tmIdAgg = (ParsedLongTerms) map.get("tmIdAgg"); //使用别名
        List<SearchResponseTmVo> tmVoList = tmIdAgg.getBuckets().stream().map(o -> {
            SearchResponseTmVo searchResponseTmVo = new SearchResponseTmVo();
            Long keyAsNumber = (Long) ((Terms.Bucket) o).getKeyAsNumber();
            searchResponseTmVo.setTmId(keyAsNumber);
            ParsedStringTerms tmNameAgg = ((Terms.Bucket) o).getAggregations().get("tmNameAgg");
            searchResponseTmVo.setTmName(tmNameAgg.getBuckets().get(0).getKeyAsString());
            ParsedStringTerms tmLogoUrlAgg = ((Terms.Bucket) o).getAggregations().get("tmLogoUrlAgg");
            searchResponseTmVo.setTmLogoUrl(tmLogoUrlAgg.getBuckets().get(0).getKeyAsString());
            return searchResponseTmVo;
        }).collect(Collectors.toList());
        vo.setTrademarkList(tmVoList);
        ParsedNested attrsAgg = (ParsedNested) map.get("attrsAgg");
        ParsedLongTerms attrIdAgg = attrsAgg.getAggregations().get("attrIdAgg");

        List<SearchResponseAttrVo> attrsList = attrIdAgg.getBuckets().stream().map(o -> {
            SearchResponseAttrVo attrVo = new SearchResponseAttrVo();
            String s = ((Terms.Bucket) o).getKeyAsString();
            attrVo.setAttrId(Long.parseLong(s));
            ParsedStringTerms attrNameAgg = ((Terms.Bucket) o).getAggregations().get("attrNameAgg");
            attrVo.setAttrName(attrNameAgg.getBuckets().get(0).getKeyAsString());
            ParsedStringTerms attrValueAgg = ((Terms.Bucket) o).getAggregations().get("attrValueAgg");
            attrVo.setAttrValueList(attrValueAgg.getBuckets().stream().map(o1 -> {
                return ((Terms.Bucket) o1).getKeyAsString();
            }).collect(Collectors.toList()));
            return attrVo;
        }).collect(Collectors.toList());
        vo.setAttrsList(attrsList);
        //解析结果集
        SearchHits hits = search.getHits();
        //总记录数
        vo.setTotal(hits.getTotalHits());
        SearchHit[] searchHits = hits.getHits();
        if (searchHits.length > 0 && searchHits != null) {
            //商品结果集
            List<Goods> goodsList = Arrays.stream(searchHits).map(documentFields -> {
                String sourceAsString = documentFields.getSourceAsString();//获取到字符创
                Goods goods = JSONObject.parseObject(sourceAsString, Goods.class);
                Map<String, HighlightField> highlightFields = documentFields.getHighlightFields();
                HighlightField title = highlightFields.get("title");
                if (title != null) {
                    //有高亮名称 优先使用高亮名称   没有使用原来 的普通名称
                    goods.setTitle(title.fragments()[0].toString());
                }
                return goods;
            }).collect(Collectors.toList());
            vo.setGoodsList(goodsList);

        }
        return vo;
    }

    private SearchSourceBuilder buildSourceBuilder(SearchParam searchParam) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        //组合条件对象
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        if (!StringUtils.isEmpty(searchParam.getKeyword())) { //必填项 不填就使用默认的  可以自定义
            boolQueryBuilder.must(QueryBuilders.matchQuery("title", searchParam.getKeyword()).operator(Operator.AND));  //分词

        } else {
            boolQueryBuilder.must(QueryBuilders.matchAllQuery());
        }

        //2 品牌 品牌ID:品牌的名称
        if (!StringUtils.isEmpty(searchParam.getTrademark())) { //精确匹配 termQuery
            String[] split = StringUtils.split(searchParam.getTrademark(), ":");//分割  只能在2个字符之间使用
            boolQueryBuilder.must(QueryBuilders.termQuery("tmId", split[0]));
        }

        //3.三级分类
        Long category1Id = searchParam.getCategory1Id();
        if (category1Id != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("category1Id", category1Id));
        }
        Long category2Id = searchParam.getCategory2Id();
        if (category1Id != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("category2Id", category2Id));
        }
        Long category3Id = searchParam.getCategory3Id();
        if (category1Id != null) {
            boolQueryBuilder.must(QueryBuilders.termQuery("category3Id", category3Id));
        }

        //4.平台属性集合
        String[] props = searchParam.getProps();
        if (null != props && props.length > 0) {
            //这里面又是一个新的组合对象
            BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
            Arrays.stream(props).map(prop -> {
                // prop =  平台属性的ID:平台属性值的名称:平台属性的名称
                String[] p = prop.split(":");
                BoolQueryBuilder subBoolQuery = QueryBuilders.boolQuery();
                subBoolQuery.must(QueryBuilders.termQuery("attrs.attrId", p[0]));
                subBoolQuery.must(QueryBuilders.termQuery("attrs.attrValue", p[1]));
                return boolQuery.must(QueryBuilders.nestedQuery("attrs", subBoolQuery, ScoreMode.None));
            }).collect(Collectors.toList());
            boolQueryBuilder.must(boolQuery);
        }
        searchSourceBuilder.query(boolQueryBuilder);

        //5.排序
        String order = searchParam.getOrder();
        //（综合 1） 价格2) （新品3）  order=1:desc 或 1:asc
        if (!StringUtils.isEmpty(order)) {
            String[] o = StringUtils.split(order, ":");
            String s = "";
            switch (o[0]) {
                case "1":
                    s = "hotScore";
                    break;
                case "2":
                    s = "price";
                    break;
                case "3":
                    s = "createTime";
                    break;
            }
            searchSourceBuilder.sort(s, o[1].equalsIgnoreCase("desc") ? SortOrder.DESC : SortOrder.ASC);

        } else {//按照默认 排序 即一般哪找热度排序
            searchSourceBuilder.sort("hotScore", SortOrder.DESC);
        }

        //6.分页
        Integer pageNo = searchParam.getPageNo();
        Integer pageSize = searchParam.getPageSize();
        searchSourceBuilder.from((pageNo - 1) * pageSize);//开始行
        searchSourceBuilder.size(pageSize);//每页数

        //7.高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title").preTags("<font style='color:red'>").postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);

        //8.品牌聚合
        searchSourceBuilder.aggregation(AggregationBuilders.terms("tmIdAgg").field("tmId")
                .subAggregation(AggregationBuilders.terms("tmNameAgg").field("tmName"))
                .subAggregation(AggregationBuilders.terms("tmLogoUrlAgg").field("tmLogoUrl")));

        //9.平台属性聚合
        searchSourceBuilder.aggregation(AggregationBuilders.nested("attrsAgg", "attrs")
                .subAggregation(AggregationBuilders.terms("attrIdAgg").field("attrs.attrId") //一父两儿子
                        .subAggregation(AggregationBuilders.terms("attrNameAgg").field("attrs.attrName"))
                        .subAggregation(AggregationBuilders.terms("attrValueAgg").field("attrs.attrValue"))));
        return searchSourceBuilder;
    }

}
