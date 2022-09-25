package com.offcn.search.service;

import java.util.Map;

/**
 * Created by travelround on 2021/3/30.
 */
public interface SkuService {

    /***
     * 导入SKU数据，也就是将审核状态为1，通过审核从数据库中查询出来的商品放进到ES索引中（ES服务器，放检索商品的微服务）
     */
    void importSku();

    /***
     * 搜索
     * @param searchMap
     * @return
     */
    Map search(Map<String, String> searchMap);
}