package com.qingcheng.service.impl;

import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Init implements InitializingBean {

    @Autowired
    private CategoryService categoryService;
    @Autowired
    private SkuService skuService;

    //初始化加入容器中
    public void afterPropertiesSet() throws Exception {
        System.out.println("初始化缓存，商品缓存预热");
        categoryService.saveCategoryTreeToRedis();
        System.out.println("所有的商品价格缓存预热");
        skuService.saveAllPriceToRedis();
        //初始化加载所有商品
        skuService.importToEs();

    }


}
