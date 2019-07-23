package com.qingcheng.pojo.goods;

import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
@Table(name = "tb_category_brand")
public class CategoryBrand implements Serializable {
    @Id
    private Integer CategorId;
    @Id
    private Integer brandId;

    public Integer getCategorId() {
        return CategorId;
    }

    public void setCategorId(Integer categorId) {
        CategorId = categorId;
    }

    public Integer getBrandId() {
        return brandId;
    }

    public void setBrandId(Integer brandId) {
        this.brandId = brandId;
    }
}
