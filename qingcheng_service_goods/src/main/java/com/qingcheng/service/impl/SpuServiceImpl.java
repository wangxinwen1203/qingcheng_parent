package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.CategoryBrandMapper;
import com.qingcheng.dao.CategoryMapper;
import com.qingcheng.dao.SkuMapper;
import com.qingcheng.dao.SpuMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.*;
import com.qingcheng.service.goods.SkuService;
import com.qingcheng.service.goods.SpuService;
import com.qingcheng.util.CacheKey;
import com.qingcheng.util.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service(interfaceClass = SpuService.class)
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private IdWorker idWorker;
    @Autowired
    private CategoryMapper categoryMapper;
    @Autowired
    private CategoryBrandMapper categoryBrandMapper;
    @Autowired
    private SkuService skuService;

    /**
     * 返回全部记录
     *
     * @return
     */
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 分页查询
     *
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Spu> findPage(int page, int size) {
        PageHelper.startPage(page, size);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectAll();
        return new PageResult<Spu>(spus.getTotal(), spus.getResult());
    }

    /**
     * 条件查询
     *
     * @param searchMap 查询条件
     * @return
     */
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     *
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page, size);
        Example example = createExample(searchMap);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectByExample(example);
        return new PageResult<Spu>(spus.getTotal(), spus.getResult());
    }

    /**
     * 根据Id查询
     *
     * @param id
     * @return
     */
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     *
     * @param spu
     */
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 修改
     *
     * @param spu
     */
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 删除
     *
     * @param id
     */
    public void delete(String id) {


        spuMapper.deleteByPrimaryKey(id);
    }

    @Transactional
    public void saveGoods(Goods goods) {


        //保存一个spu
        Spu spu = goods.getSpu();
        if (spu.getId() == null) {
            long l = idWorker.nextId();
            spu.setId(l + "");
            spuMapper.insert(spu);
        } else {
            //修改,先删除在修改
            Example example = new Example(Sku.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("spuId", spu.getId());
            skuMapper.deleteByExample(example);
            spuMapper.updateByPrimaryKeySelective(spu);
        }

        Date date = new Date();
        //分类对象
        Category category = categoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        //保存所有的List<sku>信息
        List<Sku> skuList = goods.getSkuList();
        for (Sku sku : skuList) {
            if (sku.getId() == null) {//新增
                sku.setId(idWorker.nextId() + "");
                sku.setSpuId(spu.getId());
                sku.setCreateTime(date);
            }
            //传过来的规格为空的
            if (sku.getSpec() == null || "".equals(sku.getSpec())) {
                sku.setSpec("{}");//也是JSON格式
            }
            //sku名称=spu+列表名称
            String name = spu.getName();
            String spec = sku.getSpec();

            //把传过来的字符串转化为json格式，在把json格式转为map对象进行取值，就拿到了属性值
            Map<String, String> map = JSON.parseObject(spec, Map.class);
            Collection<String> values = map.values();
            for (String value : values) {
                name += value;
            }
            sku.setName(name);

            sku.setUpdateTime(date);
            sku.setCategoryId(spu.getCategory3Id());//分类ID
            sku.setCategoryName(category.getName());//分类名称
            sku.setCommentNum(0);//评论数
            sku.setSaleNum(0);//销售数量
            // sku.setSpuId(spu.getId());
            skuMapper.insert(sku);
            //重新将价格更新到缓存
            skuService.savePriceToRedisById(sku.getId(),sku.getPrice());

        }

        // 建立分类与Brand的关联

        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrandId(spu.getBrandId());
        categoryBrand.setCategorId(spu.getCategory3Id());
        int count = categoryBrandMapper.selectCount(categoryBrand);
        if (count == 0) {
            categoryBrandMapper.insert(categoryBrand);
        }
    }

    public Goods findGoodsById(String id) {
        //查询spu
        Spu spu = spuMapper.selectByPrimaryKey(id);
        //查询List<Sku>
        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", id);
        List<Sku> skus = skuMapper.selectByExample(example);
        //封装成Goods
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skus);
        return goods;
    }

    public void audit(String id, String status, String message) {
        //修改审核状态换人上架状态
        Spu spu = new Spu();
        spu.setId(id);
        spu.setStatus(status);
        if ("1".equals(status)) {
            spu.setIsMarketable("1");
        }
        spuMapper.updateByPrimaryKeySelective(spu);

        //2.记录商品审核记录
//        创建数据库表、实体类、接口、insert实现
        //3.记录商品日志
    }

    /**
     * 商品下架
     *
     * @param id
     */
    public void pull(String id) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 商品上架
     *
     * @param id
     */
    public void put(String id) {
        //修改状态
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if (!"1".equals(spu.getStatus())) {
            throw new RuntimeException("商品审核未通过");
        }
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);


    }

    /**
     * 批量上架
     * @param ids
     * @return
     */
    public int putMany(String[] ids) {
        Spu spu = new Spu();
        spu.setIsMarketable("1");

        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andIn("id",Arrays.asList(ids));
        criteria.andEqualTo("idMarketable","0");//未上架的
        criteria.andEqualTo("status","1");//审核通过的

        int i = spuMapper.updateByExampleSelective(spu, example);
        //1.添加日志信息
        return i;
    }

    /**
     * 逻辑删除
     * @param id
     */
    public void deleteById(String id) {


        Spu spu = new Spu();
        spu.setId(id);
        spu.setIsDelete("1");
        spuMapper.updateByPrimaryKeySelective(spu);
        //删除缓存中的价格数据
        Map map=new HashMap();
        map.put("spuId",id);
        List<Sku> list = skuService.findList(map);
        for (Sku sku : list) {
           skuService.deletePriceFromRedis(sku.getId());
        }


    }
    //商品下架


    /**
     * 构建查询条件
     *
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap) {
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if (searchMap != null) {
            // 主键
            if (searchMap.get("id") != null && !"".equals(searchMap.get("id"))) {
                criteria.andLike("id", "%" + searchMap.get("id") + "%");
            }
            // 货号
            if (searchMap.get("sn") != null && !"".equals(searchMap.get("sn"))) {
                criteria.andLike("sn", "%" + searchMap.get("sn") + "%");
            }
            // SPU名
            if (searchMap.get("name") != null && !"".equals(searchMap.get("name"))) {
                criteria.andLike("name", "%" + searchMap.get("name") + "%");
            }
            // 副标题
            if (searchMap.get("caption") != null && !"".equals(searchMap.get("caption"))) {
                criteria.andLike("caption", "%" + searchMap.get("caption") + "%");
            }
            // 图片
            if (searchMap.get("image") != null && !"".equals(searchMap.get("image"))) {
                criteria.andLike("image", "%" + searchMap.get("image") + "%");
            }
            // 图片列表
            if (searchMap.get("images") != null && !"".equals(searchMap.get("images"))) {
                criteria.andLike("images", "%" + searchMap.get("images") + "%");
            }
            // 售后服务
            if (searchMap.get("saleService") != null && !"".equals(searchMap.get("saleService"))) {
                criteria.andLike("saleService", "%" + searchMap.get("saleService") + "%");
            }
            // 介绍
            if (searchMap.get("introduction") != null && !"".equals(searchMap.get("introduction"))) {
                criteria.andLike("introduction", "%" + searchMap.get("introduction") + "%");
            }
            // 规格列表
            if (searchMap.get("specItems") != null && !"".equals(searchMap.get("specItems"))) {
                criteria.andLike("specItems", "%" + searchMap.get("specItems") + "%");
            }
            // 参数列表
            if (searchMap.get("paraItems") != null && !"".equals(searchMap.get("paraItems"))) {
                criteria.andLike("paraItems", "%" + searchMap.get("paraItems") + "%");
            }
            // 是否上架
            if (searchMap.get("isMarketable") != null && !"".equals(searchMap.get("isMarketable"))) {
                criteria.andLike("isMarketable", "%" + searchMap.get("isMarketable") + "%");
            }
            // 是否启用规格
            if (searchMap.get("isEnableSpec") != null && !"".equals(searchMap.get("isEnableSpec"))) {
                criteria.andLike("isEnableSpec", "%" + searchMap.get("isEnableSpec") + "%");
            }
            // 是否删除
            if (searchMap.get("isDelete") != null && !"".equals(searchMap.get("isDelete"))) {
                criteria.andLike("isDelete", "%" + searchMap.get("isDelete") + "%");
            }
            // 审核状态
            if (searchMap.get("status") != null && !"".equals(searchMap.get("status"))) {
                criteria.andLike("status", "%" + searchMap.get("status") + "%");
            }

            // 品牌ID
            if (searchMap.get("brandId") != null) {
                criteria.andEqualTo("brandId", searchMap.get("brandId"));
            }
            // 一级分类
            if (searchMap.get("category1Id") != null) {
                criteria.andEqualTo("category1Id", searchMap.get("category1Id"));
            }
            // 二级分类
            if (searchMap.get("category2Id") != null) {
                criteria.andEqualTo("category2Id", searchMap.get("category2Id"));
            }
            // 三级分类
            if (searchMap.get("category3Id") != null) {
                criteria.andEqualTo("category3Id", searchMap.get("category3Id"));
            }
            // 模板ID
            if (searchMap.get("templateId") != null) {
                criteria.andEqualTo("templateId", searchMap.get("templateId"));
            }
            // 运费模板id
            if (searchMap.get("freightId") != null) {
                criteria.andEqualTo("freightId", searchMap.get("freightId"));
            }
            // 销量
            if (searchMap.get("saleNum") != null) {
                criteria.andEqualTo("saleNum", searchMap.get("saleNum"));
            }
            // 评论数
            if (searchMap.get("commentNum") != null) {
                criteria.andEqualTo("commentNum", searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
