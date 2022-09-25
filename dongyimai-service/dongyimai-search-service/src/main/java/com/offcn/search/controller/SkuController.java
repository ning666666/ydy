package com.offcn.search.controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.search.service.SkuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Created by travelround on 2021/3/30.
 */
@RestController
@RequestMapping(value = "/search")
@CrossOrigin
public class SkuController {

    @Autowired
    private SkuService skuService;

    /**
     * 导入sku数据
     * @return
     */
    @GetMapping("/import")
    public Result search(){
        skuService.importSku();
        return new Result(true, StatusCode.OK,"导入数据到索引库中成功！");
    }

    /**
     * 搜索
     * @param searchMap
     * @return
     */
    @GetMapping
    public Map search(@RequestParam(required = false) Map searchMap){
        //传的参数设置为map集合是因为后续会按关键字搜索、页面条件搜索、分类搜索
        //返回值也为map集合的JSON字符串，不设置为list的原因是，list集合一般放统一的一类数据，比如查出来的实体类集合，
        //前台可以直接设置数组来接收展示在页面，而设置为map集合因为需要传的数据有表示商品信息的对象、分页数据、条件查询的数据等，
        //map集合的JSON数据前台也好调用，也可以用result包起来
        return skuService.search(searchMap);

        //这里需要从es索引中查询数据，而es安装在虚拟机上，所以需要启动es
    }
}
