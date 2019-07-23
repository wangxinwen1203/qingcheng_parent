package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.*;
import com.qingcheng.pojo.system.*;
import com.qingcheng.service.system.AdminRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;

@Service(interfaceClass = AdminRoleService.class)
public class AdminRoleImpl implements AdminRoleService {
    @Autowired
    private AdminMapper adminMapper;
    @Autowired
    private RoleMapper roleMapper;
    @Autowired
    private AdminRoleMapper adminRoleMapper;
    @Autowired
    private RoleResourceMapper roleResourceMapper;
    @Autowired
    private ResourceMapper resourceMapper;

    @Transactional
    //添加用户及角色
    public void add(GroupAdminRole groupAdminRole) {

        AdminRole adminRole = new AdminRole();
        Admin admin = groupAdminRole.getAdmin();
        //加密处理
        String password = admin.getPassword();
        String hashpw = BCrypt.hashpw(password, BCrypt.gensalt());
        admin.setPassword(hashpw);

        List<Integer> roleIds = groupAdminRole.getRoleIds();
        adminMapper.insert(admin);
        Integer id = admin.getId();//获取到用户的id
        for (Integer roleId : roleIds) {//遍历用户拥有的所有的角色
            Role role = roleMapper.selectByPrimaryKey(roleId);
            roleMapper.insert(role);
            adminRole.setAdminId(id);
            adminRole.setRoleId(roleId);//分别插入到中间表中
        }


    }

    //根据id去查找该用户信息及所有的角色
    public GroupAdminRole findAdminRoleById(Integer id) {
        Admin admin = adminMapper.selectByPrimaryKey(id);
//        admin.setPassword(null);//把密码设为空再去返回
        Example example = new Example(Role.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("id", id);
        List<Role> roles = roleMapper.selectByExample(example);//查找到所有的角色
        //拿到所有的角色ids
        List<Integer> list = new ArrayList();
        for (Role role : roles) {
            list.add(role.getId());
        }
        GroupAdminRole groupAdminRole = new GroupAdminRole();
        groupAdminRole.setAdmin(admin);
        groupAdminRole.setRoleIds(list);

        return groupAdminRole;
    }

    @Transactional
    //修改用户及角色信息
    public void updateAdminRole(GroupAdminRole groupAdminRole) {
        Integer adminId = groupAdminRole.getAdmin().getId();
        Example example = new Example(AdminRole.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("adminId", adminId);
        //删除中间表adminRole
        adminRoleMapper.deleteByExample(example);
        //再次调用增加方法
        add(groupAdminRole);
    }

    @Transactional//权限控制，给用户更新权限
    public void saveRoleVO(RoleVO roleVO) {
        Integer id = roleVO.getId();
        //封装role和resource的中间表
        RoleResource roleResource = new RoleResource();
        roleResource.setRoleId(id);
        List<Resource> resources = roleVO.getResources();
        for (Resource resource : resources) {
            Integer resourceId = resource.getId();
            roleResource.setResourceId(resourceId);
            //插入中间表
            roleResourceMapper.insertSelective(roleResource);
        }
    }
    //根据角色的id去查询RoleVO
    public RoleVO findRoleVOById(Integer id){
        Example example = new Example(RoleResource.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("resourceId",id);
        List<RoleResource> roleResources = roleResourceMapper.selectByExample(example);

        List<Resource> list = new ArrayList();
        for (RoleResource roleResource : roleResources) {
            Resource resource = resourceMapper.selectByPrimaryKey( roleResource.getResourceId());
            list.add(resource);
        }
        Role role = roleMapper.selectByPrimaryKey(id);
        String name = role.getName();
        RoleVO roleVO = new RoleVO();
        roleVO.setResources(list);
        roleVO.setId(id);
        roleVO.setName(name);

        return roleVO;
    }

}
