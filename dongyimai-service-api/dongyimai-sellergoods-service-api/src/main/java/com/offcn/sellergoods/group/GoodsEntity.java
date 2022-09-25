package com.offcn.sellergoods.group;

import com.offcn.sellergoods.pojo.Goods;
import com.offcn.sellergoods.pojo.GoodsDesc;
import com.offcn.sellergoods.pojo.Item;

import java.io.Serializable;
import java.util.List;

public class GoodsEntity implements Serializable {
    private Goods goods; //商品信息 SPU(公共属性)
    private GoodsDesc goodsDesc; //商品扩展信息
    private List<Item> itemList; //商品信息 SKU（独有属性）

    public Goods getGoods() {
        return goods;
    }

    public void setGoods(Goods goods) {
        this.goods = goods;
    }

    public GoodsDesc getGoodsDesc() {
        return goodsDesc;
    }

    public void setGoodsDesc(GoodsDesc goodsDesc) {
        this.goodsDesc = goodsDesc;
    }

    public List<Item> getItemList() {
        return itemList;
    }

    public void setItemList(List<Item> itemList) {
        this.itemList = itemList;
    }

    @Override
    public String toString() {
        return "GoodsEntity{" +
                "goods=" + goods +
                ", goodsDesc=" + goodsDesc +
                ", itemList=" + itemList +
                '}';
    }
}
