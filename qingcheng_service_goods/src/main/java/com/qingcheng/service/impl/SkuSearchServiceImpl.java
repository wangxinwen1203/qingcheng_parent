package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.BrandMapper;
import com.qingcheng.dao.SpecMapper;
import com.qingcheng.service.goods.SkuSearchService;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.*;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class SkuSearchServiceImpl implements SkuSearchService {

    /*  HttpHost  :  url地址封装

      RestClientBuilder： rest客户端构建器

      RestHighLevelClient： rest高级客户端

      IndexRequest： 新增或修改请求

      IndexResponse：新增或修改的响应结果*/
    //1.连接rest接口
    @Autowired
    private RestHighLevelClient restHighLevelClient;
    @Autowired
    private BrandMapper brandMapper;
    @Autowired
    private SpecMapper specMapper;

    public Map search(Map<String, String> searchMap) {

        //1.封装查询请求
        SearchRequest searchRequest = new SearchRequest("sku");
        searchRequest.types("doc"); //设置查询的类型
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();//布尔查询构建器
        MatchQueryBuilder matchQueryBuilder = QueryBuilders.matchQuery("name", searchMap.get("keywords"));
        boolQueryBuilder.must(matchQueryBuilder);
//        1.2商品分类过滤
        if (searchMap.get("category") != null) {
            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("categoryName", searchMap.get("category"));
            boolQueryBuilder.filter(termQueryBuilder);
        }


        //添加分页效果，计算总页数
        int pageSize = 30;
        int pageNo = Integer.parseInt(searchMap.get("pageNo"));
        int pageIndex = (pageNo - 1) * pageSize;
        searchSourceBuilder.from(pageIndex);
        searchSourceBuilder.size(pageSize);


//        searchSourceBuilder.sort("price", SortOrder.valueOf("asd"));//按价格升序排序
        String sort = searchMap.get("sort");//排序字段
        String sortOrder = searchMap.get("sortOrder");//排序规则
        if (!"".equals(sort)) {
            searchSourceBuilder.sort(sort, SortOrder.valueOf(sortOrder));
        }
        //修改关键字高亮设置
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("name").preTags("<font style='color:red'>").postTags("</font>");
        searchSourceBuilder.highlighter(highlightBuilder);


//        1.3品牌过滤
        if (searchMap.get("brand") != null) {

            TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("brandName", searchMap.get("brand"));
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //1.4规格过滤
        for (String key : searchMap.keySet()) {
            if (key.startsWith("spec.")) {
                TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(key + ".keyword", searchMap.get(key));
                boolQueryBuilder.filter(termQueryBuilder);
            }
        }

        searchSourceBuilder.query(boolQueryBuilder);
//        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(10000).lte(20000);
//        searchSourceBuilder.query(rangeQueryBuilder);

        //1.5 价格过滤
        if (searchMap.get("price") != null) {
            String[] price = searchMap.get("price").split("-");
            if (!price[0].equals("0")) { //最低价格不等于0
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").gte(price[0] + "00");
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
            if (!price[1].equals("*")) { //如果价格由上限
                RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("price").lte(price[1] + "00");
                boolQueryBuilder.filter(rangeQueryBuilder);
            }
        }
        searchRequest.source(searchSourceBuilder);
        //分类聚合查询（商品分类）
        TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("sku_category").field("categoryName");
        searchSourceBuilder.aggregation(termsAggregationBuilder);

        //2.封装查询结果
        Map resultMap = new HashMap();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            SearchHits searchHits = searchResponse.getHits();

            //拿到总的查询记录数
            long totalCount = searchHits.getTotalHits();
            //计算总页数
            long totalPages = ((totalCount % pageSize) == 0 ? totalCount / pageSize : totalCount / pageSize + 1);
            resultMap.put("totalPages", totalPages);

            System.out.println("记录数：" + totalCount);
            SearchHit[] hits = searchHits.getHits();
            Aggregations aggregations = searchResponse.getAggregations();
            Map<String, Aggregation> asMap = aggregations.getAsMap();
            Terms terms = (Terms) asMap.get("sku_category");//通过键拿到商品的品牌集合
            List<? extends Terms.Bucket> buckets = terms.getBuckets();
            ArrayList<String> categoryList = new ArrayList<String>();
            for (Terms.Bucket bucket : buckets) {
                categoryList.add(bucket.getKeyAsString());
            }
            resultMap.put("categoryList", categoryList);//拿到商品分类的结果

            //2.1商品列表
            List<Map<String, Object>> mapList = new ArrayList();
            for (SearchHit hit : hits) {
                Map<String, Object> skuMap = hit.getSourceAsMap();
                //设置高亮效果
                Map<String, HighlightField> highlightFields = hit.getHighlightFields();
                HighlightField name = highlightFields.get("name");
                Text[] fragments = name.fragments();
                String s = fragments[0].toString();
                skuMap.put("name",s);
                mapList.add(skuMap);
            }
            resultMap.put("rows", mapList);


            String categoryName = "";
            //2.3品牌列表
            if (searchMap.get("brand") == null) {

                if (searchMap.get("category") == null) {
                    if (categoryList.size() > 0) {
                        categoryName = categoryList.get(0);
                    }
                } else {
                    categoryName = searchMap.get("category");
                }
               /* if(searchMap.get("category")==null){ // 如果没有分类条件
                    if(categoryList.size()>0){
                        categoryName=categoryList.get(0);//提取分类列表的第一个分类
                    }
                }else{
                    categoryName=searchMap.get("category");//取出参数中的分类
                }*/
                List<Map> brandList = brandMapper.findListByCategoryName(categoryName);
                resultMap.put("brandList", brandList);
            }
            List<Map> specList = specMapper.findListByCategoryName(categoryName);
            //规格列表
            for (Map spec : specList) {
                String[] options = ((String) spec.get("options")).split(",");
                spec.put("options", options);
            }
            resultMap.put("specList", specList);


        } catch (IOException e) {
            e.printStackTrace();
        }

        return resultMap;
    }
}
