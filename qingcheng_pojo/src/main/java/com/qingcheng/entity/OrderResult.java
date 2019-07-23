package com.qingcheng.entity;

import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderItem;

import java.io.Serializable;
import java.util.List;

public class OrderResult implements Serializable{
    private Order order;
    private List<OrderItem> orderItemList;

    public OrderResult() {
    }

    public OrderResult(Order order, List<OrderItem> orderItemList) {
        this.order = order;
        this.orderItemList = orderItemList;
    }

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
