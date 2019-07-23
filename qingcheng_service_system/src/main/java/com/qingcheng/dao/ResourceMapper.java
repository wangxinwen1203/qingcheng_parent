package com.qingcheng.dao;

import com.qingcheng.pojo.system.Resource;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface ResourceMapper extends Mapper<Resource> {
    //根据姓名去查询所有的资源权限
    @Select(" SELECT res_key FROM tb_resource WHERE id IN" +
            " ( SELECT resource_id FROM tb_role_resource WHERE role_id IN" +
            " (SELECT role_id FROM tb_admin_role WHERE admin_id IN" +
            "(SELECT id FROM tb_admin WHERE login_name=#{name}) ))")
    List<String> findAllResKey(String name);

}
