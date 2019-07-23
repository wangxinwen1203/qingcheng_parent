package com.qingcheng.pojo.system;

import java.io.Serializable;
import java.util.List;

public class RoleVO implements Serializable{
    private Integer id;
    private String name;
    private List<Resource> resources;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Resource> getResources() {
        return resources;
    }

    public void setResources(List<Resource> resources) {
        this.resources = resources;
    }
}
