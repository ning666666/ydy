package com.offcn.search.pojo;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;

/**
 * Created by travelround on 2021/3/30.
 */
@Document(indexName = "skuinfo", type = "docs") //skuinfo 云上的索引名
public class SkuInfo implements Serializable { //同tb_item表，sku表
    //商品id，同时也是商品编号
    @Id
    private Long id;

    //SKU名称，ik_smart拆词力度
    @Field(type = FieldType.Text, analyzer = "ik_smart")
    private String title;

    //商品价格，单位为：元 浮点类型，只能比较大小关系，不能比较相等，只能是无限接近，BigDecimal无穷无限接近，减少误差，BigDecimal在es上是不识别的
    @Field(type = FieldType.Double)
    private BigDecimal price;

    //库存数量
    private Integer num;

    //商品图片
    private String image;

    //商品状态，1-正常，2-下架，3-删除
    private String status;

    //创建时间
    private Date createTime;

    //更新时间
    private Date updateTime;

    //是否默认
    private String isDefault;

    //goodsId
    private Long goodsId;

    //类目ID
    private Long categoryId;

    //类目名称，不拆词
    @Field(type = FieldType.Keyword)
    private String category;

    //品牌名称
    @Field(type = FieldType.Keyword)
    private String brand;

    //规格
    private String spec;

    //规格参数，取spec，JSON字符串的值，方便检索，上面的brand之类的，以brand做键，对应的品牌是能查出来的
    //而spec是记录sku独有属性的JSON字符串，直接以spec做键是查不出来的，所以添加map属性，方便检索
    private Map<String, Object> specMap;

    //对应的get、set方法省略

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getIsDefault() {
        return isDefault;
    }

    public void setIsDefault(String isDefault) {
        this.isDefault = isDefault;
    }

    public Long getGoodsId() {
        return goodsId;
    }

    public void setGoodsId(Long goodsId) {
        this.goodsId = goodsId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getSpec() {
        return spec;
    }

    public void setSpec(String spec) {
        this.spec = spec;
    }

    public Map<String, Object> getSpecMap() {
        return specMap;
    }

    public void setSpecMap(Map<String, Object> specMap) {
        this.specMap = specMap;
    }

    @Override
    public String toString() {
        return "SkuInfo{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", price=" + price +
                ", num=" + num +
                ", image='" + image + '\'' +
                ", status='" + status + '\'' +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", isDefault='" + isDefault + '\'' +
                ", goodsId=" + goodsId +
                ", categoryId=" + categoryId +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", spec='" + spec + '\'' +
                ", specMap=" + specMap +
                '}';
    }
}
