package com.offcn.item.service.impl;

import com.alibaba.fastjson.JSON;
import com.offcn.entity.Result;
import com.offcn.item.service.PageService;
import com.offcn.sellergoods.feign.GoodsFeign;
import com.offcn.sellergoods.feign.ItemCatFeign;
import com.offcn.sellergoods.group.GoodsEntity;
import com.offcn.sellergoods.pojo.Goods;
import com.offcn.sellergoods.pojo.GoodsDesc;
import com.offcn.sellergoods.pojo.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PageServiceImpl implements PageService {

    @Autowired
    private GoodsFeign goodsFeign;

    @Autowired
    private ItemCatFeign itemCatFeign;

    @Autowired //页面静态化技术需要的对象
    private TemplateEngine templateEngine;

    //生成静态文件路径
    @Value("${pagepath}")
    private String pagepath;


    /**
     * 构建数据模型
     *
     * @param goodsId
     * @return
     */
    private Map<String, Object> buildDataModel(Long goodsId) {
        //构建数据模型，保存前端需要的数据
        Map<String, Object> dataMap = new HashMap<>();
        //获取SPU 和SKU列表
        Result<GoodsEntity> result = goodsFeign.findById(goodsId);
        GoodsEntity goodsEntity = result.getData();
        //1.加载SPU数据
        Goods goods = goodsEntity.getGoods();
        //2.加载商品扩展数据
        GoodsDesc goodsDesc = goodsEntity.getGoodsDesc();

        //3.加载SKU数据
        List<Item> itemList = goodsEntity.getItemList();

        dataMap.put("goods", goods);

        dataMap.put("goodsDesc", goodsDesc);
        dataMap.put("specificationList", JSON.parseArray(goodsDesc.getSpecificationItems(),Map.class));
        dataMap.put("imageList",JSON.parseArray(goodsDesc.getItemImages(),Map.class));
        dataMap.put("itemList",itemList);

        //4.加载分类数据
        dataMap.put("category1",itemCatFeign.findById(goods.getCategory1Id()).getData());
        dataMap.put("category2",itemCatFeign.findById(goods.getCategory2Id()).getData());
        dataMap.put("category3",itemCatFeign.findById(goods.getCategory3Id()).getData());


        return dataMap;
    }

    /**根据商品的ID 生成静态页
     *
     * @param spuId
     * @return
     */
    @Override
    public boolean createPageHtml(Long goodsId) {
        // 1.上下文，为了取数据
        Context context = new Context();
        Map<String, Object> dataModel = buildDataModel(goodsId);
        context.setVariables(dataModel);
        // 2.准备文件
        File dir = new File(pagepath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File dest = new File(dir, goodsId + ".html");
        // 3.生成页面
        try (PrintWriter writer = new PrintWriter(dest, "UTF-8")) {
            //                               模板    上下文，A放了B也能取到  IO
            templateEngine.process("item", context, writer);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
