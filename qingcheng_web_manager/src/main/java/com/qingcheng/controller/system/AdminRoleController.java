package com.qingcheng.controller.system;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.system.GroupAdminRole;
import com.qingcheng.pojo.system.Resource;
import com.qingcheng.pojo.system.RoleVO;
import com.qingcheng.service.system.AdminRoleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/adminRole")
public class AdminRoleController {

    @Reference
    private AdminRoleService adminRoleService;
    @PostMapping("/save")//添加用户以及其角色
    public void save(@RequestBody GroupAdminRole groupAdminRole){

        adminRoleService.add(groupAdminRole);
    }
    //修改先根据传过来的用户id查找所有的信息以及角色
    @GetMapping("/findAdminROleById")
    public GroupAdminRole findAdminRoleById(Integer id){
        GroupAdminRole groupAdminRole=adminRoleService.findAdminRoleById(id);

        return groupAdminRole;
    }
    @PostMapping("/updateAdminRole")
    public void updateAdminRole(@RequestBody GroupAdminRole groupAdminRole){
        adminRoleService.updateAdminRole(groupAdminRole);

    }
    @PostMapping("/saveAdminResource")//修改权限控制
    public void saveAdminResource(RoleVO roleVO){
        adminRoleService.saveRoleVO(roleVO);

    }
    @GetMapping("/findRoleVOById")//根据角色名去获得所有的权限
    public RoleVO findROleVOById(Integer id){
        RoleVO roleVO = adminRoleService.findRoleVOById(id);

        return roleVO;
    }



}
