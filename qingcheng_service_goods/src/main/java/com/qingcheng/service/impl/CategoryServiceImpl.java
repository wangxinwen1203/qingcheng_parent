package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.CategoryMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Category;
import com.qingcheng.service.goods.CategoryService;
import com.qingcheng.util.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 返回全部记录
     *
     * @return
     */
    public List<Category> findAll() {
        return categoryMapper.selectAll();
    }

    /**
     * 分页查询
     *
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Category> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        Page<Category> categorys = (Page<Category>) categoryMapper.selectAll();
        return new PageResult<Category>(categorys.getTotal(), categorys.getResult());
    }

    /**
     * 条件查询
     *
     * @param searchMap 查询条件
     * @return
     */
    public List<Category> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return categoryMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     *
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Category> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        Page<Category> categorys = (Page<Category>) categoryMapper.selectByExample(example);
        return new PageResult<Category>(categorys.getTotal(), categorys.getResult());
    }

    /**
     * 根据Id查询
     *
     * @param id
     * @return
     */
    public Category findById(Integer id) {
        return categoryMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     *
     * @param category
     */
    public void add(Category category) {

        categoryMapper.insert(category);
//增加数据后再次调用保存到缓存数据中
       saveCategoryTreeToRedis();
    }

    /**
     * 修改
     *
     * @param category
     */
    public void update(Category category) {

        categoryMapper.updateByPrimaryKeySelective(category);
        //更新后再次调用存入缓存
        saveCategoryTreeToRedis();
    }

    /**
     * 删除
     *
     * @param id
     */
    public void delete(Integer id) {
        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("parentId", id);
        int i = categoryMapper.selectCountByExample(example);
        if (i > 0) {
            throw new RuntimeException("存在下级，不能删除");
        }
        categoryMapper.deleteByPrimaryKey(id);
        //删除后再次调用存入缓存
        saveCategoryTreeToRedis();
    }

    public List<Map> findCategoryTree() {

        //从缓存中查询
        List<Map> mapList = (List<Map>) redisTemplate.boundValueOps(CacheKey.CATEGORY_TREE).get();
        System.out.println("从缓存中查询商品");

        return mapList;
    }

    public void saveCategoryTreeToRedis() {
        //1.先从数据库中查询
        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("isShow", "1");
        List<Category> categories = categoryMapper.selectByExample(example);
        example.setOrderByClause("seq");
        List<Map> categoryTree = findByParentId(categories, 0);
        //2.再把查询到额数据存到缓存中
        redisTemplate.boundValueOps(CacheKey.CATEGORY_TREE).set(categoryTree);
    }

    private List<Map> findByParentId(List<Category> categories, Integer parentId) {
        List<Map> list = new ArrayList<Map>();
        for (Category category : categories) {
            if (parentId.equals(category.getParentId())) {
                Map map = new HashMap();
                map.put("name", category.getName());
                map.put("menu", findByParentId(categories, category.getId()));
                list.add(map);
            }
        }
        return list;

    }

    /**
     * 构建查询条件
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 分类名称
            if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
                criteria.andLike("name", "%" + searchMap.get("name") + "%");
            }
            // 是否显示
            if (searchMap.get("isShow") != null && !"".equals(searchMap.get("isShow"))) {
                criteria.andLike("isShow", "%" + searchMap.get("isShow") + "%");
            }
            // 是否导航
            if (searchMap.get("isMenu") != null && !"".equals(searchMap.get("isMenu"))) {
                criteria.andLike("isMenu", "%" + searchMap.get("isMenu") + "%");
            }

            // 分类ID
            if (searchMap.get("id") != null) {
                criteria.andEqualTo("id", searchMap.get("id"));
            }
            // 商品数量
            if (searchMap.get("goodsNum") != null) {
                criteria.andEqualTo("goodsNum", searchMap.get("goodsNum"));
            }
            // 排序
            if (searchMap.get("seq") != null) {
                criteria.andEqualTo("seq", searchMap.get("seq"));
            }
            // 上级ID
            if (searchMap.get("parentId") != null) {
                criteria.andEqualTo("parentId", searchMap.get("parentId"));
            }
            // 模板ID
            if (searchMap.get("templateId") != null) {
                criteria.andEqualTo("templateId", searchMap.get("templateId"));
            }

        }
        return example;
    }

}
