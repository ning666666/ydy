package com.offcn.sellergoods.feign;

import com.offcn.entity.Result;
import com.offcn.sellergoods.pojo.Item;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 将通过审核状态查询商品sku的远程服务，注册到注册中心，也可以写到search需要使用的订阅方去，
 * 但是每个远程调用的应用，或者说提供的服务，建议还是放在对应的业务逻辑处理层模块，方便统一管理
 */
@FeignClient(name = "dym-sellergoods")
@RequestMapping(value = "/item")
public interface ItemFeign {

    /***
     * 根据审核状态查询Sku
     * @param status
     * @return
     */
    @GetMapping("/status/{status}")
//PathVariable是将绑定到占位符的参数和指定参数绑定
    Result<List<Item>> findByStatus(@PathVariable(value = "status") String status);

    /***
     * 根据ID查询Item数据
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    Result<Item> findById(@PathVariable(value = "id") Long id);

    /***
     * 库存递减
     * @param username
     * @return
     */
    @PostMapping(value = "/decr/count")
    Result decrCount(@RequestParam(value = "username") String username);
}
