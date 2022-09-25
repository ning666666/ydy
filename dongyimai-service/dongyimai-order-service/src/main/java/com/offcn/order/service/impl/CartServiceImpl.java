package com.offcn.order.service.impl;

import com.offcn.entity.Result;
import com.offcn.order.pojo.Cart;
import com.offcn.order.pojo.OrderItem;
import com.offcn.order.service.CartService;
import com.offcn.sellergoods.feign.ItemFeign;
import com.offcn.sellergoods.pojo.Item;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by travelround on 2021/6/23.
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private ItemFeign itemFeign;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public List<Cart> addGoodsToCartList(List<Cart> cartList, Long itemId, Integer num) {

        //1.根据商品SKU ID查询SKU商品信息
        Result<Item> itemResult = itemFeign.findById(itemId);
        Item item = itemResult.getData();
        if (item == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!item.getStatus().equals("1")) {
            throw new RuntimeException("商品状态无效");
        }

        //2.获取商家ID
        String sellerId = item.getSellerId();

        //3.根据商家ID判断购物车列表中是否存在该商家的购物车
        Cart cart = searchCartBySellerId(cartList, sellerId);

        //4.如果购物车列表中不存在该商家的购物车
        if (cart == null) {

            //4.1 新建购物车对象 ，
            cart = new Cart();
            cart.setSellerId(sellerId);
            cart.setSellerName(item.getSeller());
            OrderItem orderItem = createOrderItem(item, num);//获取购物车中的商品数据
            List orderItemList = new ArrayList();
            orderItemList.add(orderItem);//往购物车中添加商品数据
            cart.setOrderItemList(orderItemList);

            //4.2将购物车对象添加到购物车列表
            cartList.add(cart);

        } else {
            //购物车列表--购物车（商家id、商家、商品信息集合）--购物车明细列表（有哪些商品）--商品明细
            //5.如果购物车列表中存在该商家的购物车
            // 判断购物车明细列表中是否存在该商品
            OrderItem orderItem = searchOrderItemByItemId(cart.getOrderItemList(), itemId);
            if (orderItem == null) {
                //5.1. 如果没有，新增购物车（商品）明细
                orderItem = createOrderItem(item, num);
                cart.getOrderItemList().add(orderItem);
            } else {
                //5.2. 如果有，在原购物车明细上添加数量，更改金额
                orderItem.setNum(orderItem.getNum() + num);
                orderItem.setTotalFee(new BigDecimal(orderItem.getPrice().doubleValue() * orderItem.getNum()));

                //如果数量操作后小于等于0，则移除
                if (orderItem.getNum() <= 0) {
                    cart.getOrderItemList().remove(orderItem);//移除购物车明细，移出商品数据
                }
                //如果移除后cart的明细数量为0，则将cart移除
                if (cart.getOrderItemList().size() == 0) {
                    cartList.remove(cart);
                }
            }
        }
        return cartList;
    }

    /**
     * 从redis中查询购物车
     *
     * @param username
     * @return
     */
    @Override
    public List<Cart> findCartListFromRedis(String username) {
        System.out.println("从redis中提取购物车数据....." + username);
        List<Cart> cartList = (List<Cart>) redisTemplate.boundHashOps("cartList").get(username);
        if (cartList == null) {
            cartList = new ArrayList();
        }
        return cartList;

    }

    /**
     * 将购物车保存到redis
     *
     * @param username
     * @param cartList
     */
    @Override
    public void saveCartListToRedis(String username, List<Cart> cartList) {
        System.out.println("向redis存入购物车数据....." + username);
        redisTemplate.boundHashOps("cartList").put(username, cartList);

    }

    /**
     * 根据商家ID查询购物车对象
     *
     * @param cartList
     * @param sellerId
     * @return
     */
    private Cart searchCartBySellerId(List<Cart> cartList, String sellerId) {
        for (Cart cart : cartList) {
            if (cart.getSellerId().equals(sellerId)) {
                return cart;
            }
        }
        return null;
    }

    /**
     * 根据商品明细ID查询
     *
     * @param orderItemList
     * @param itemId
     * @return
     */
    private OrderItem searchOrderItemByItemId(List<OrderItem> orderItemList, Long itemId) {
        for (OrderItem orderItem : orderItemList) {
            if (orderItem.getItemId().longValue() == itemId.longValue()) {
                return orderItem;
            }
        }
        return null;
    }

    /**
     * 创建商品明细
     *
     * @param item
     * @param num
     * @return
     */
    private OrderItem createOrderItem(Item item, Integer num) {
        if (num <= 0) {
            throw new RuntimeException("数量非法");
        }

        OrderItem orderItem = new OrderItem();
        orderItem.setGoodsId(item.getGoodsId());
        orderItem.setItemId(item.getId());
        orderItem.setNum(num);
        orderItem.setPicPath(item.getImage());
        orderItem.setPrice(new BigDecimal(item.getPrice()));
        orderItem.setSellerId(item.getSellerId());
        orderItem.setTitle(item.getTitle());
        orderItem.setTotalFee(new BigDecimal(num * Double.parseDouble(item.getPrice())));
        return orderItem;
    }

}