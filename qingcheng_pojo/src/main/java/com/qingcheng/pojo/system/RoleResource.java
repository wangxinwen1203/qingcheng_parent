package com.qingcheng.pojo.system;

import javax.persistence.Id;
import javax.persistence.Table;

@Table(name = "tb_role_resource")
public class RoleResource {
    @Id
    private Integer resourceId;
    @Id
    private Integer roleId;

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }
}
