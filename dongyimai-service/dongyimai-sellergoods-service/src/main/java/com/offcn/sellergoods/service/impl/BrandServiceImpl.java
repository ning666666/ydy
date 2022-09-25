package com.offcn.sellergoods.service.impl;

import com.alibaba.druid.util.StringUtils;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.entity.PageResult;
import com.offcn.sellergoods.dao.BrandMapper;
import com.offcn.sellergoods.pojo.Brand;
import com.offcn.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class BrandServiceImpl extends ServiceImpl<BrandMapper, Brand> implements BrandService {
    /*@Autowired
    private BrandMapper brandMapper; === extends ServiceImpl<BrandMapper, Brand>*/
    //IService接口进一步封装了BaseMapper，ServiceImpl还能带上具体的实体类类型，
    //所以选择继承ServiceImpl，替代从IOC容器中直接获取具体的Mapper对象
    @Override
    public List<Brand> findAll() {
        return this.list(); //加this强调，可不加----- 同brandMapper.findAll()
    }

    @Override
    public Brand findById(Long id) {
        return this.getById(id);
    }

    @Override
    public void add(Brand brand) {
        this.save(brand);
    }

    @Override
    public void update(Brand brand) {
        this.updateById(brand);
    }

    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    @Override
    public List<Brand> findList(Brand brand) {
        QueryWrapper<Brand> queryWrapper = this.createQueryWrapper(brand);
        //根据条件查询
        return this.list(queryWrapper);
    }

    @Override
    public PageResult<Brand> findPage(int page, int size) {
        // com.baomidou.mybatisplus.extension.plugins.pagination.Page
        Page<Brand> mypage = new Page<>(page, size); //分页查询需要传的参数，结果是总记录数和当前页结果
        IPage<Brand> iPage = this.page(mypage, new QueryWrapper<Brand>());
        return new PageResult<Brand>(iPage.getTotal(), iPage.getRecords());
    }

    @Override
    public PageResult<Brand> findPage(Brand brand, int page, int size) {
        Page<Brand> mypage = new Page<>(page, size);
        QueryWrapper<Brand> queryWrapper = this.createQueryWrapper(brand);
        IPage<Brand> iPage = this.page(mypage, queryWrapper);
        return new PageResult<Brand>(iPage.getTotal(), iPage.getRecords());
    }

    //抽取条件查询构造器类，返回添加好条件的条件构造器对象
    private QueryWrapper<Brand> createQueryWrapper(Brand brand) {
        QueryWrapper<Brand> queryWrapper = new QueryWrapper<>();
        if (brand != null) {
            if (brand.getId() != null) {
                queryWrapper.eq("id", brand.getId());
            }
            //品牌名称
            // org.springframework.util.StringUtils
            if (!StringUtils.isEmpty(brand.getName())) {
                queryWrapper.like("name", brand.getName());
            }
            //品牌首字母
            if (!StringUtils.isEmpty(brand.getFirstChar())) {
                queryWrapper.eq("first_char", brand.getFirstChar());
            }
            //品牌图片
            if (!StringUtils.isEmpty(brand.getImage())) {
                queryWrapper.eq("image", brand.getImage());
            }
        }
        return queryWrapper;
    }
    @Autowired
    private BrandMapper brandMapper;
    @Override
    public List<Map> selectOptions() {
        return brandMapper.selectOptions();
    }
}