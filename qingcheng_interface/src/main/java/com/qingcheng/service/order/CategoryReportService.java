package com.qingcheng.service.order;

import com.qingcheng.pojo.order.CategoryReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CategoryReportService {
    List<CategoryReport> categoryReport(LocalDate localDate);

    public void createReport();
    public  List<Map>  categoryCount(String date1, String date2);

}
