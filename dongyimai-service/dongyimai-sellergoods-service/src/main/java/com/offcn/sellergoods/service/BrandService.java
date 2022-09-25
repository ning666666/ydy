package com.offcn.sellergoods.service;

import com.offcn.entity.PageResult;
import com.offcn.sellergoods.pojo.Brand;

import java.util.List;
import java.util.Map;

public interface BrandService {
    /**
     * 查询所有品牌
     * @return
     */
    List<Brand> findAll();

    /**
     * 根据Id查询品牌
     * @param id
     * @return
     */
    Brand findById(Long id);

    /**
     * 增加品牌
     * @param brand
     */
    void add(Brand brand);

    /**
     * 修改品牌
     * @param brand
     */
    void update(Brand brand);

    /**
     * 删除品牌
     * @param id
     */
    void delete(Long id);

    /**
     * 多条件查询品牌
     * @param brand
     * @return
     */
    List<Brand> findList(Brand brand);

    /**
     * 分页查询
     * @param page
     * @param size
     * @return
     */
    PageResult<Brand> findPage(int page, int size);

    /**
     * 多条件分页查询
     * @param brand
     * @param page
     * @param size
     * @return
     */
    PageResult<Brand> findPage(Brand brand, int page, int size);

    /**
     * 运营商，模块管理页的新增编辑和修改的关联品牌展示，供用户选择自己商品关联的品牌
     * @return
     */
    List<Map> selectOptions();

}
