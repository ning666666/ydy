package com.offcn.search.dao;

import com.offcn.search.pojo.SkuInfo;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

/**
 * Created by travelround on 2021/3/30.
 */
@Repository
public interface SkuEsMapper extends ElasticsearchRepository<SkuInfo,Long> {
    //继承ElasticsearchRepository，复杂的条件查询实现不了
    //这里是为了实现将数据库中已经审核通过的商品，放入到ES索引库中
}
