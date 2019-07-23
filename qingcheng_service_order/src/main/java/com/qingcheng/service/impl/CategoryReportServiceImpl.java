package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.CategoryReportMapper;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = CategoryReportService.class)
public class CategoryReportServiceImpl implements CategoryReportService{
    @Autowired
    private CategoryReportMapper categoryReportMapper;
    @Override
    public List<CategoryReport> categoryReport(LocalDate date) {
        List<CategoryReport> categoryReportList = categoryReportMapper.categoryReport(date);
        return categoryReportList;
    }

    @Override
    @Transactional
    public void createReport() {
        LocalDate date=LocalDate.now().minusDays(1);
        List<CategoryReport> categoryReports = categoryReportMapper.categoryReport(date);//根据日期去查询
        for (CategoryReport categoryReport : categoryReports) {
            categoryReportMapper.insert(categoryReport);
        }
    }

    @Override
    public  List<Map>  categoryCount(String date1, String date2) {
        List<Map> mapList = categoryReportMapper.categoryCount(date1, date2);
        return mapList;
    }


}
