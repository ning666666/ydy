package com.offcn.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.entity.Result;
import com.offcn.search.dao.SkuEsMapper;
import com.offcn.search.pojo.SkuInfo;
import com.offcn.search.service.SkuService;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.sellergoods.pojo.Item;
import org.apache.commons.lang.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.*;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by travelround on 2021/3/30.
 */

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    private SkuEsMapper skuEsMapper;

    @Autowired
    private ItemFeign itemFeign;

    @Override
    public void importSku() {

        //调用商品微服务，获取sku商品数据
        Result<List<Item>> result = itemFeign.findByStatus("1");
        System.out.println("result = " + result);
        //把数据转换为搜索实体类数据
        //result.getData()==查询出来的item实体类list集合，调用JSON.toJSONString转为了JSON字符串，
        //list集合转为JSON字符串变成了JSon字符串数组，调用JSON.parseArray又将JSon字符串数组转为了sku具体商品信息的list集合
        List<SkuInfo> skuInfoList = JSON.parseArray(JSON.toJSONString(result.getData()), SkuInfo.class);
        //String string = JSON.toJSONString(result.getData());
        //遍历sku集合
        for (SkuInfo skuInfo : skuInfoList) {
            //获取规格，JSON.parseObject将JSON字符串装为map集合
            Map<String, Object> specMap = JSON.parseObject(skuInfo.getSpec());
            //关联设置到specMap
            skuInfo.setSpecMap(specMap);
        }
        System.out.println("bbb:" + skuInfoList);
        //保存sku集合数据到es
        skuEsMapper.saveAll(skuInfoList);
        System.out.println("ccc");
    }


    @Autowired
    private ElasticsearchRestTemplate esRestTemplateRest;//注入es操作类

    public Map search(Map<String, String> searchMap) {
        //1.获取关键字的值
        String keywords = searchMap.get("keywords");
        // org.apache.commons.lang.StringUtils
        if (StringUtils.isEmpty(keywords)) {
            keywords = "华为"; //赋值给keywords一个默认的值
        }
        //2.创建查询对象 的构建对象
        NativeSearchQueryBuilder nativeSearchQueryBuilder = new NativeSearchQueryBuilder();
        //3.设置查询的条件


        /****************************************** 新增-开始 *******************************************/
        //1)、设置分组条件(按什么去分组) 按照商品分类进行分组，在关键字基础上进行分组
        nativeSearchQueryBuilder.addAggregation(
                // AggregationBuilders聚合条件构造器
                // terms("skuCategorygroup")：给列取别名
                // field("category")：字段名称 注意,若添加分组代码后报错,一般是版本不匹配引起的代码不同,尝试改成field("category.keyword") 或 field("category.keywords")
                // size 指定查询结果的数量 默认是10个
                AggregationBuilders.terms("skuCategorygroup")
                        .field("category.keyword")
                        .size(50));
        //2)、设置品牌分组条件
        nativeSearchQueryBuilder.addAggregation(
                AggregationBuilders.terms("skuBrandgroup")
                        .field("brand.keyword")
                        .size(50));
        //3)、设置规格分组条件
        nativeSearchQueryBuilder.addAggregation(
                AggregationBuilders.terms("skuSpecgroup")
                        .field("spec.keyword")
                        .size(100));
        /****************************************** 新增-结束 *******************************************/
        /*
          1)、根据关键字搜索，QueryBuilders.matchQuery("title", keywords)表示使用关键字到哪个字段进行查询，可以多匹配
         使用：QueryBuilders.matchQuery("title", keywords) ，搜索华为 ---> 华 为 二字可以拆分查询，
         使用：QueryBuilders.matchPhraseQuery("title", keywords) 华为二字不拆分查询
         设置主关键字查询,修改为多字段的搜索条件
        nativeSearchQueryBuilder.withQuery(QueryBuilders.multiMatchQuery(keywords,"title","brand","category"));
         */
        nativeSearchQueryBuilder.withQuery(QueryBuilders.matchQuery("title", keywords));
        /*
        2)、根据分类和品牌搜索，过滤查询建议使用filter ,它的搜索效率要优于must，例如输入品牌名或分类名，就只查品牌名或分类名的数据
        */
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();//获取查询条件构建对象
        if (!StringUtils.isEmpty(searchMap.get("brand"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("brand.keyword", searchMap.get("brand")));//往构建对象中添加条件
        }
        if (!StringUtils.isEmpty(searchMap.get("category"))) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category.keyword", searchMap.get("category")));
        }
        nativeSearchQueryBuilder.withFilter(boolQueryBuilder);
        //3)、规格过滤查询
        if (searchMap != null) {
            for (String key : searchMap.keySet()) {
                if (key.startsWith("spec_")) {                                          //"spec_机身内存"，specMap实体类属性为一个map集合
                    boolQueryBuilder.filter(QueryBuilders.termQuery("specMap." + key.substring(5) + ".keyword", searchMap.get(key)));
                    System.out.println("specMap." + key.substring(5) + ".keyword");
                }
            }
        }
        //4)、价格过滤查询
        String price = searchMap.get("price");
        if (!StringUtils.isEmpty(price)) {
            //以-进行拆分
            String[] split = price.split("-");
            //判断上限是否为*
            if (!split[1].equalsIgnoreCase("*")) {
                //指定price字段进行范围查询，从哪开始和从哪结束，从下限开始，上限结束，true表示包含节点，大于等于下限，小于等于上限
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").from(split[0], true).to(split[1], true));
            } else {                                                                     //大于等于下限
                boolQueryBuilder.filter(QueryBuilders.rangeQuery("price").gte(split[0]));
            }
        }
        //5)、分页查询
        Integer pageNum = 1;
        if (!StringUtils.isEmpty(searchMap.get("pageNum"))) {
            try {
                pageNum = Integer.valueOf(searchMap.get("pageNum"));
            } catch (NumberFormatException e) {
                e.printStackTrace();
                pageNum = 1;
            }
        }
        Integer pageSize = 3;
        nativeSearchQueryBuilder.withPageable(PageRequest.of(pageNum - 1, pageSize));//计算机第一页默认为0
        //6)、排序查询
        //获取升序还是降序
        String sortRule = searchMap.get("sortRule");
        //获取给哪个字段进行升序还是降序（desc）
        String sortField = searchMap.get("sortField");
        if (!StringUtils.isEmpty(sortRule) && !StringUtils.isEmpty(sortField)) {
            nativeSearchQueryBuilder.withSort(SortBuilders.fieldSort(sortField).order(sortRule.equals("DESC") ? SortOrder.DESC : SortOrder.ASC));
        }
        //7)、关键字高亮
        //设置高亮条件，指定高亮到哪个字段匹配，有s可以拼多个
        nativeSearchQueryBuilder.withHighlightFields(new HighlightBuilder.Field("title"));
        //前置便签和后置标签
        nativeSearchQueryBuilder.withHighlightBuilder(new HighlightBuilder().preTags("<em style=\"color:red\">").postTags("</em>"));


        //4.构建查询对象
        NativeSearchQuery query = nativeSearchQueryBuilder.build();
        //5. 执行搜索，获取封装响应数据结果的SearchHits集合
        SearchHits<SkuInfo> searchHits = esRestTemplateRest.search(query, SkuInfo.class);
        //对搜索searchHits集合进行分页封装
        SearchPage<SkuInfo> skuPage = SearchHitSupport.searchPageFor(searchHits, query.getPageable());
        //高亮结果
        List<SkuInfo> skuList = new ArrayList<>();
        for (SearchHit<SkuInfo> searchHit : skuPage.getContent()) {// 获取搜索到的数据，实体类的集合数据
            SkuInfo content = (SkuInfo) searchHit.getContent();//实体类数据
            SkuInfo skuInfo = new SkuInfo();
            //使用工具类BeanUtils拷贝属性，根据属性命名相同的原则，将第一个对象属性值拷贝到第二个对象
            BeanUtils.copyProperties(content, skuInfo);
            // 处理高亮，获取高亮字段，域字段
            Map<String, List<String>> highlightFields = searchHit.getHighlightFields();
            System.out.println("highlightFields = " + highlightFields);
            //highlightFields = {title=[TCL 老人<em style="color:red">手</em><em style="color:red">机</em> (i310) 暗夜黑 移动联通2G<em style="color:red">手</em><em style="color:red">机</em>]}
            //List<String>，list集合内是拼接好的字符串
            for (Map.Entry<String, List<String>> stringHighlightFieldEntry : highlightFields.entrySet()) {
                String key = stringHighlightFieldEntry.getKey();
                if (StringUtils.equals(key, "title")) {
                    List<String> fragments = stringHighlightFieldEntry.getValue();
                    StringBuilder sb = new StringBuilder();
                    for (String fragment : fragments) {
                        sb.append(fragment.toString());
                    }
                    skuInfo.setTitle(sb.toString());
                }
            }
            skuList.add(skuInfo);
        }

        /****************************************** 新增-开始 *******************************************/
        //1)、获取分类分组结果，分组结果:结合关键字再进行分组的结果，SELECT category FROM  tb_item WHERE title LIKE '%三星%' GROUP BY category;
        //返回分组对象，searchHits里面包含根据关键字查询的到的所有sku商品信息的list集合，还包含了具体的分组结果
        //getAggregations()包含所有分组结果，上面写的目的也是为了取别名skuCategorygroup对应的分类分组结果
        Terms terms = searchHits.getAggregations().get("skuCategorygroup");
        // 获取分类名称集合
        List<String> categoryList = new ArrayList<>();
        if (terms != null) {
            for (Terms.Bucket bucket : terms.getBuckets()) { //通过分组对象.getBuckets()获取分组结果[“手机”，“平板”]
                String keyAsString = bucket.getKeyAsString();//分组的值（分类名称），以字符串形式返回
                categoryList.add(keyAsString);
            }
        }
        //2)、获取品牌分组的结果，返回分组对象
        Terms termsBrand = searchHits.getAggregations().get("skuBrandgroup");
        List<String> brandList = getStringsBrandList(termsBrand);
        //3)、获取规格分组的结果，返回分组对象
        Terms termsSpec = searchHits.getAggregations().get("skuSpecgroup");
        //分组对象获取分组结果，Set集合去重复
        Map<String, Set<String>> specMap = getStringSetMap(termsSpec);
        /****************************************** 新增-结束 *******************************************/

        //6.返回结果
        Map resultMap = new HashMap<>();

        /****************************************** 新增-开始 *******************************************/
        resultMap.put("categoryList", categoryList);//分类分组数据
        resultMap.put("brandList", brandList);//品牌分组数据
        resultMap.put("specMap", specMap);//规格分组数据
        resultMap.put("rows", skuList);//获取所需SkuInfo集合数据内容
        /****************************************** 新增-结束 *******************************************/

        //resultMap.put("rows", skuPage.getContent());//获取所需SkuInfo集合数据内容
        resultMap.put("total", skuPage.getTotalElements());//总记录数
        resultMap.put("totalPages", skuPage.getTotalPages());//总页数
        //分页数据保存
        //设置当前页码
        resultMap.put("pageNum", pageNum);
        resultMap.put("pageSize", 30);
        return resultMap;
    }

    /**
     * 获取规格列表
     *
     * @param termsSpec
     * @return
     */
    private Map<String, Set<String>> getStringSetMap(Terms termsSpec) {
        /*1.获取所有规格数据
        2.将所有规格数据转换成Map
        3.定义一个Map<String,Set>,key是规格名字，防止重复所以用Map，valu是规格值，规格值有多个，所以用集合，为了防止规格重复，用Set去除重复
        4.循环规格的Map，将数据填充到定义的Map<String,Set>中*/
        Map<String, Set<String>> specMap = new HashMap<String, Set<String>>();
        Set<String> specList = new HashSet<>();
        if (termsSpec != null) {
            //1.如果规格信息不为空，获取所有规格数据，并将数据保存进set集合去重
            //[{"机身内存":"16G","网络":"联通3G"},{"机身内存":"16G","网络":"联通2G"},、、、]
            for (Terms.Bucket bucket : termsSpec.getBuckets()) {
                specList.add(bucket.getKeyAsString());
            }
        }
        //2.遍历保存规格信息的set集合，将规格数据转为map集合，Map.class最终的结果字节码对象
        for (String specjson : specList) {
            //{"机身内存"="16G","网络"="联通3G"}
            Map<String, String> map = JSON.parseObject(specjson, Map.class);
            //3.遍历Map<String, String>集合，将Map<String, String>数据填充到Map<String,Set>,key中
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();        //规格名字
                String value = entry.getValue();    //规格选项值
                //获取当前规格名字对应的规格数据
                Set<String> specValues = specMap.get(key);
                if (specValues == null) {
                    specValues = new HashSet<String>();
                }
                //将当前规格加入到集合中
                specValues.add(value);
                //将数据存入到specMap中
                //{"机身内存"=["16G","32"],"网络"=["联通2G","联通3G"]}
                specMap.put(key, specValues);
            }
        }
        return specMap;
    }

    /**
     * 获取品牌列表
     *
     * @param termsBrand
     * @return
     */
    private List<String> getStringsBrandList(Terms termsBrand) {
        List<String> brandList = new ArrayList<>();
        if (termsBrand != null) {
            for (Terms.Bucket bucket : termsBrand.getBuckets()) {
                brandList.add(bucket.getKeyAsString());
            }
        }
        return brandList;
    }


    /**
     * 获取分类列表数据
     *
     * @param terms
     * @return
     */
    private List<String> getStringsCategoryList(Terms terms) {
        List<String> categoryList = new ArrayList<>();
        if (terms != null) {
            for (Terms.Bucket bucket : terms.getBuckets()) {
                String keyAsString = bucket.getKeyAsString();//分组的值
                categoryList.add(keyAsString);
            }
        }
        return categoryList;
    }
}

