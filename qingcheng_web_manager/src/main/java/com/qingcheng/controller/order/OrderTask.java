package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.order.CategoryReportService;
import com.qingcheng.service.order.OrderService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderTask {
    @Reference
    private OrderService orderService;
    @Scheduled(cron = "0 0/2 * * * ?")//每两分钟执行一次
    public  void orderTimeOutLogic(){
        orderService.orderTimeOutLogic();
    }


    @Reference
    private CategoryReportService categoryReportService;
    @Scheduled(cron = "0 0 1 * * ?")
    public void createReport(){
        categoryReportService.createReport();
    }

}
