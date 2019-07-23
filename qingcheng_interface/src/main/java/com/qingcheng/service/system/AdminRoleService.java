package com.qingcheng.service.system;

import com.qingcheng.pojo.system.GroupAdminRole;
import com.qingcheng.pojo.system.RoleVO;

public interface AdminRoleService {
    public void add(GroupAdminRole groupAdminRole);

    GroupAdminRole findAdminRoleById(Integer id);

    void updateAdminRole(GroupAdminRole groupAdminRole);

    void saveRoleVO(RoleVO roleVO);
    //根据角色id去查询所有的权限
    RoleVO findRoleVOById(Integer id);
}
