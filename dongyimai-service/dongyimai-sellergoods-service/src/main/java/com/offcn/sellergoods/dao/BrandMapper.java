package com.offcn.sellergoods.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offcn.sellergoods.pojo.Brand;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

//1、mybatisPlus采用代码或者 Maven 插件可快速生成，Mapper 、 Model 、 Service 、 Controller 层代码，支持模板引擎
//2、接口的继承是在整合多个接口的规则进来
//3、mapper的接口映射文件都是可以省略的
//这里手动继承BaseMapper的原因是没有使用逆向工程生成的
public interface BrandMapper extends BaseMapper<Brand> {
    @Select("select id,name as text from tb_brand")
    List<Map> selectOptions();
}
//sellergoods商家商品信息