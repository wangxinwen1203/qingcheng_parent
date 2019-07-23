package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.pojo.goods.Category;
import com.qingcheng.pojo.goods.Sku;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.order.CartService;
import com.qingcheng.service.order.PreferentialService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public List<Map<String, Object>> findCartList(String username) {
        System.out.println("从购物车中提取" + username);
        List<Map<String, Object>> cartList = (List<Map<String, Object>>) redisTemplate.boundHashOps(CacheKey.Cart_List).get(username);
        if (cartList == null) {
            cartList = new ArrayList<>();
        }
        return cartList;
    }

    /**
     * 添加商品到购物车，或者删除，或移除
     *
     * @param username
     * @param skuId
     * @param num
     */
    @Reference
    private SkuService skuService;
    @Reference
    private CategoryService categoryService;

    @Override
    public void addItem(String username, String skuId, Integer num) {

        //实现思路，遍历购物车，如果购物车存在则累加，不存在则添加到购物车
        //获取购物车
        boolean flag = false;//是否在购物车中存在
        List<Map<String, Object>> cartList = findCartList(username);
        for (Map map : cartList) {
            OrderItem orderItem = (OrderItem) map.get("item");
            if (orderItem.getSkuId().equals(skuId)) {//说明已存在

                if (orderItem.getNum() <= 0) {
                    cartList.remove(map);
                    flag = true;
                    break;
                }

                int weight = orderItem.getWeight() / orderItem.getNum();

                orderItem.setNum(orderItem.getNum() + num);//数量变更
                if (orderItem.getNum() <= 0) { //如果数量小于等于0
                    cartList.remove(map);//购物车项删除
                }

                orderItem.setMoney(orderItem.getPrice() * orderItem.getNum());//金额变更
                orderItem.setWeight(weight * orderItem.getNum());


                flag = true;
                break;
            }
        }
        if (flag == false) {//商品不存在，直接添加
            Sku sku = skuService.findById(skuId);
            if (sku == null) {
                throw new RuntimeException("该商品不存在");
            }
            if (!"1".equals(sku.getStatus())) {
                throw new RuntimeException("商品状态不合法");

            }
            if (num < 0) {
                throw new RuntimeException("商品数量不合法");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setCategoryId3(sku.getCategoryId());

            Category category3 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(sku.getCategoryId());
            if (category3 == null) {
                category3 = categoryService.findById(sku.getCategoryId());//根据3级id查找2级
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(sku.getCategoryId(), category3);
            }

            orderItem.setCategoryId2(category3.getParentId());

            Category category2 = (Category) redisTemplate.boundHashOps(CacheKey.CATEGORY).get(category3.getParentId());
            if (category2 == null) {
                category2 = categoryService.findById(category3.getParentId());//根据2级id查询1级
                redisTemplate.boundHashOps(CacheKey.CATEGORY).put(category3.getParentId(), category2);
            }

            orderItem.setCategoryId1(category2.getParentId());

//            BeanUtils.copyProperties(orderItem,sku);
            orderItem.setSkuId(skuId);
            orderItem.setSpuId(sku.getSpuId());
            orderItem.setImage(sku.getImage());
            orderItem.setNum(num);
            orderItem.setPrice(sku.getPrice());
            orderItem.setName(sku.getName());
            orderItem.setMoney(sku.getPrice() * num);
            if (sku.getWeight() == null) {
                sku.setWeight(0);
            }
            orderItem.setWeight(sku.getWeight() *num);


            Map map = new HashMap<String, Object>();
            map.put("item", orderItem);
            map.put("checked", true);//默认选中
            cartList.add(map);

        }
        redisTemplate.boundHashOps(CacheKey.Cart_List).put(username, cartList);

    }

    @Override
    public boolean updateChecked(String username, String skuId, boolean checked) {
        List<Map<String, Object>> cartList = findCartList(username);

        boolean isOk=false;
        for (Map<String, Object> map : cartList) {
            OrderItem item = (OrderItem) map.get("item");
            if(item.getSkuId().equals(skuId)){
                map.put("checked",checked);
                isOk=true;
                break;
            }
        }
        if(isOk){
            redisTemplate.boundHashOps(CacheKey.Cart_List).put(username,cartList);
        }

        return isOk;
    }

    /**
     * 删除选中的购物车
     * @param username
     */
    @Override
    public void deleteCheckedCart(String username) {
        //获得未选中的购物车
        List<Map<String, Object>> cartList = findCartList(username).stream().filter(cart -> (boolean) cart.get("checked") == false)
                .collect(Collectors.toList());
        redisTemplate.boundHashOps(CacheKey.Cart_List).put(username,cartList);//把未勾选的代替前面所有的，存到Redis


    }
    /**
     * 计算购物车的优惠金额
     */
    @Autowired
    private PreferentialService preferentialService;
    @Override
    public int preferential(String username) {
        //获取选中的购物车
        List<Map<String, Object>> checkedList = findCartList(username).stream().filter(cart -> (boolean) cart.get("checked") == true)
                .collect(Collectors.toList());
        //按分类聚合统计每个分类金额
        int preMoney=0;
        for (Map<String, Object> map : checkedList) {
            OrderItem item = (OrderItem) map.get("item");
            Integer categoryId = item.getCategoryId3();
            Integer money = item.getMoney();
            preMoney += preferentialService.findPreMoneyByCategoryId(categoryId, money);
            System.out.println("分类："+categoryId+"消费金额:"+money+"优惠金额"+preMoney);
        }

        //循环金额，统计每个分类的分类金额，并累加

        return preMoney;
    }
}
