package com.offcn.sellergoods.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offcn.order.pojo.OrderItem;
import com.offcn.sellergoods.pojo.Item;
import org.apache.ibatis.annotations.Update;

/****
 * @Author:ujiuye
 * @Description:Item的Dao
 * @Date 2021/2/1 14:19
 *****/
public interface ItemMapper extends BaseMapper<Item> {
    /**
     * 递减库存，此处的itemId是订单明细orderItem中的skuId
     * @param orderItem
     * @return
     */
    @Update("UPDATE tb_item SET num=num-#{num} WHERE id=#{itemId} AND num>=#{num}")
    int decrCount(OrderItem orderItem);
}
