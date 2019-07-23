package com.qingcheng.service.order;

import java.util.List;
import java.util.Map;

public interface CartService {
//从redis中提取某用户的购物车
    public List<Map<String,Object>> findCartList(String username);

    /**
     * 添加商品到购物车，或者删除，或移除
     * @param username
     * @param skuId
     * @param num
     */
    public void addItem(String username,String skuId,Integer num);

    public boolean updateChecked(String username,String skuId,boolean checked);


    public void deleteCheckedCart(String username);

    /**
     * 计算购物车的优惠金额
     */
    public int preferential(String username);

}
