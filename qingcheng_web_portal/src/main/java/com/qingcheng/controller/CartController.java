package com.qingcheng.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.entity.Result;
import com.qingcheng.service.order.CartService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Reference
    private CartService cartService;

    @GetMapping("/findCartList")
    public List<Map<String, Object>> findCartList() {

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Map<String, Object>> cartList = cartService.findCartList(username);
        return cartList;
    }

    @GetMapping("/addItem")
    public Result addItem(String skuId, Integer num) {
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.addItem(name, skuId, num);
        return new Result();
    }

    @GetMapping("/buy")
    public void buy(HttpServletResponse response, String skuId, Integer num) throws IOException {

        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.addItem(name, skuId, num);
        response.sendRedirect("/cart.html");
    }
    @GetMapping("/updateChecked")
    public  Result updateChecked(String skuId,boolean checked){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        cartService.updateChecked(name,skuId,checked);
        return new Result();

    }

    /**
     * 删除选中的购物车
     * @return
     */
    @GetMapping("deleteCheckedCart")
    public Result deleteCheckedCart(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        cartService.deleteCheckedCart(name);
        return new Result();

    }

    /**
     * 计算购物车金额
     * @return
     */
    @GetMapping("/preferential")
    public Map preferential(){
        String name = SecurityContextHolder.getContext().getAuthentication().getName();
        int preferential = cartService.preferential(name);
        Map map=new HashMap();
        map.put("preferential",preferential);
        return map;

    }
}
