package com.qingcheng.dao;

import com.qingcheng.pojo.order.CategoryReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface CategoryReportMapper extends Mapper<CategoryReport> {
    @Select(
            "SELECT category_id1 categoryId1,category_id2 categoryId2,category_id3 categoryId3,DATE_FORMAT(o.pay_time,'%Y-%m-%d') countDate,SUM(oi.num) num,SUM(oi.pay_money) money" +
                    " FROM tb_order_item oi,tb_order o " +
                    " WHERE o.id=oi.order_id AND o.pay_status=1 AND DATE_FORMAT(o.pay_time,'%Y-%m-%d')=#{date} AND o.is_delete=0" +
                    " GROUP BY category_id1,category_id2,category_id3,DATE_FORMAT(o.pay_time,'%Y-%m-%d')")
    public List<CategoryReport> categoryReport(@Param("date") LocalDate date);

    @Select(" SELECT category_id1 categoryId1,c.name,SUM(num) num,SUM(money) money " +
            "FROM tb_category_report r,v_category c " +
            "WHERE c.id=r.category_id1 AND  count_Date>=#{date1} " +
            "AND count_Date<=#{date2}GROUP BY category_id1,c.name")
    public List<Map> categoryCount(@Param("date1") String date1,@Param("date2") String date2);


}
