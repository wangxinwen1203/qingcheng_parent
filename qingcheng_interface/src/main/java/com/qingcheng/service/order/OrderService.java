package com.qingcheng.service.order;
import com.qingcheng.entity.OrderResult;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Order;

import java.util.*;

/**
 * order业务逻辑层
 */
public interface OrderService {


    public List<Order> findAll();


    public PageResult<Order> findPage(int page, int size);


    public List<Order> findList(Map<String,Object> searchMap);


    public PageResult<Order> findPage(Map<String,Object> searchMap,int page, int size);


    public Order findById(String id);

    public void add(Order order);


    public void update(Order order);


    public void delete(String id);

    //订单详情
    public OrderResult findByOrdersId(String id);

    //批量发货 先查询所有未发货的订单
    public  List<Order> findOrdersNotSend();

    //查询被勾选的所有位发货的订单
    public List<Order> findOrderByIds(String[] ids);

    //前端点击确认后再传过来订单集合就行批量发货
    public void batchSend(List<Order> orders);

    //任务超时处理
    public void orderTimeOutLogic();

}
