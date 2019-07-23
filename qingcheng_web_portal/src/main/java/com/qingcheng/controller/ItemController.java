package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.qingcheng.pojo.goods.Goods;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.goods.Spu;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;


import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/item")
public class ItemController {

    @Reference
    private SpuService spuService;
    @Reference
    private CategoryService categoryService;
    @Autowired
    private TemplateEngine templateEngine;

    @Value("${pagePath}")
    private String pagePath;

    @GetMapping("/createPage")
    public void index(String id) {
        Goods good = spuService.findGoodsById(id);
        Spu spu = good.getSpu();
        Map<String, Object> urlMap = new HashMap<>();
        for (Sku sku : good.getSkuList()) {
          if ("1".equals(sku.getStatus())){
              String specJson = JSON.toJSONString(JSON.parseObject(sku.getSpec()), SerializerFeature.MapSortField);
              urlMap.put(specJson,sku.getId()+".html");
          }else {
              String specJson = JSON.toJSONString(JSON.parseObject(sku.getSpec()), SerializerFeature.MapSortField);
              urlMap.put(specJson,"javascript:;");
          }
        }

        for (Sku sku : good.getSkuList()) {
            ArrayList<String> categoryList = new ArrayList<>();
            Context context = new Context();

            categoryList.add(categoryService.findById(spu.getCategory1Id()).getName());
            categoryList.add(categoryService.findById(spu.getCategory2Id()).getName());
            categoryList.add(categoryService.findById(spu.getCategory3Id()).getName());
            Map paraItems = JSON.parseObject(spu.getParaItems());
            Map specItems = JSON.parseObject(sku.getSpec());


            Map<String, List> specMap = (Map<String, List>) JSON.parse(spu.getSpecItems());
            for (String key : specMap.keySet()) {
                List<String> list = specMap.get(key);
                List<Map> mapList = new ArrayList<>();
                for (String value : list) {
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("option",value);
                    JSONObject spec = JSON.parseObject(sku.getSpec());
                    spec.put(key,value);
                    String specJson= JSON.toJSONString(spec, SerializerFeature.MapSortField);
                    map.put("url",urlMap.get(specJson));

                    if (specItems.get(key).equals(value)){
                    map.put("checked",true);
                    }else {
                        map.put("checked",false);
                    }
                    mapList.add(map);
                }
                specMap.put(key,mapList);
            }


            Map<String, Object> dataModel = new HashMap<>();
            dataModel.put("specMap", specMap);
            dataModel.put("paraItems", paraItems);
            dataModel.put("specItems", specItems);
            dataModel.put("skuImages", sku.getImages().split(","));
            dataModel.put("spuImages", spu.getImages().split(","));
            dataModel.put("spu", spu);
            dataModel.put("sku", sku);
            dataModel.put("categoryList", categoryList);
            context.setVariables(dataModel);

            File file = new File(pagePath);
            if (!file.exists()) {
                file.mkdirs();
            }
            File dest = new File(file, sku.getId() + ".html");
            try {
                PrintWriter writer = new PrintWriter(dest, "UTF-8");
                templateEngine.process("item", context, writer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
