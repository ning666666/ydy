package com.offcn.sellergoods.controller;

import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.sellergoods.dao.BrandMapper;
import com.offcn.sellergoods.pojo.Brand;
import com.offcn.sellergoods.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.yaml.snakeyaml.events.Event;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/brand")
@CrossOrigin //跨域，服务器保护行为，当访问方和访问的目的方，存在三者中任何一个不一样会触发跨域保护
//域名：.com/.cn 端口号：找进程 协议：http、https、tcp等
public class BrandController {
    @Autowired
    private BrandService brandService;

    /**
     * 1.post安全加密
     * 2.get数据长度有限
     * 3.get基本类型
     */
    @GetMapping
    public Result<List<Brand>> findAll() {
        List<Brand> brandList = brandService.findAll();
        return new Result<List<Brand>>(true, StatusCode.OK, "查询成功", brandList);
    }

    /**
     * 根据id查询品牌
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result<Brand> findById(@PathVariable(value = "id", required = false) Long id) {
        //http://localhost:9001/brand/35
        //@PathVariable：从url请求地址中，获取参数值，参数值就是{id}占位符被填充的值35（填充占位符的值），赋值给指定参数
        //主键查询
        Brand brand = brandService.findById(id);
        return new Result<Brand>(true, StatusCode.OK, "查询成功", brand);
    }

    @PostMapping //加不加参数也不会冲突，因为指定了访问方式
    public Result<Brand> add(@RequestBody Brand brand) {
        brandService.add(brand);
        return new Result<Brand>(true, StatusCode.OK, "添加成功");
    }

    @PutMapping(value = "/{id}")
    public Result update(@RequestBody Brand brand, @PathVariable Long id) {
        //没必要写，因为修改之前就已经查询出来了
        //设置主键值，加id是为了支持主键也可以修改，可能有些数据表，将用户名做主键
        //默认不传的话是不修改主键的，传的话，跟着请求带进来对象一起修改
        brand.setId(id);
        brandService.update(brand);
        return new Result<Brand>(true, StatusCode.OK, "修改成功");
    }

    @DeleteMapping(value = "/{id}")
    public Result delete(@PathVariable Long id) {
        brandService.delete(id);
        return new Result(true, StatusCode.OK, "删除成功");
    }

    /**
     * 多条件查询品牌
     *
     * @param brand
     * @return
     */
    @PostMapping(value = "/search") //两个同样的请求方式，需加二级路径
    public Result<List<Brand>> findList(@RequestBody(required = false) Brand brand) {
        List<Brand> list = brandService.findList(brand);
        return new Result<List<Brand>>(true, StatusCode.OK, "查询成功", list);
    }


    /**
     * 分页查询品牌
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}")
    public Result<PageResult<Brand>> findPage(@PathVariable int page,
                                              @PathVariable int size) {
        PageResult<Brand> pageResult = brandService.findPage(page, size);
        return new Result<PageResult<Brand>>(true, StatusCode.OK, "查询成功", pageResult);
    }

    /**
     * 分页+条件查询品牌
     * @param brand
     * @param page
     * @param size
     * @return
     */
    @PostMapping(value = "/search/{page}/{size}")
    public Result<PageResult> findPage(@RequestBody(required = false) Brand brand,
                                       @PathVariable int page, @PathVariable int size) {
        PageResult<Brand> pageResult = brandService.findPage(brand, page, size);
        return new Result(true, StatusCode.OK, "查询成功", pageResult);
    }

    @GetMapping("/selectOptions")
    public ResponseEntity<List<Map>> selectOptions(){
        return ResponseEntity.ok(brandService.selectOptions());
    }
}
