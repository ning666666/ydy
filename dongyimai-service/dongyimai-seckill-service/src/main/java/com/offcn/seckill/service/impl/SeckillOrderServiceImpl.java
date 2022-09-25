package com.offcn.seckill.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.offcn.entity.PageResult;
import com.offcn.entity.StatusCode;
import com.offcn.seckill.dao.SeckillGoodsMapper;
import com.offcn.seckill.dao.SeckillOrderMapper;
import com.offcn.seckill.entity.SeckillStatus;
import com.offcn.seckill.pojo.SeckillGoods;
import com.offcn.seckill.pojo.SeckillOrder;
import com.offcn.seckill.service.SeckillOrderService;
import com.offcn.seckill.task.MultiThreadingCreateOrder;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;

/****
 * @Author:ujiuye
 * @Description:SeckillOrder业务层接口实现类
 * @Date 2021/2/1 14:19
 *****/
@Service
public class SeckillOrderServiceImpl extends ServiceImpl<SeckillOrderMapper, SeckillOrder> implements SeckillOrderService {


    /**
     * SeckillOrder条件+分页查询
     *
     * @param seckillOrder 查询条件
     * @param page         页码
     * @param size         页大小
     * @return 分页结果
     */
    @Override
    public PageResult<SeckillOrder> findPage(SeckillOrder seckillOrder, int page, int size) {
        Page<SeckillOrder> mypage = new Page<>(page, size);
        QueryWrapper<SeckillOrder> queryWrapper = this.createQueryWrapper(seckillOrder);
        IPage<SeckillOrder> iPage = this.page(mypage, queryWrapper);
        return new PageResult<SeckillOrder>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * SeckillOrder分页查询
     *
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageResult<SeckillOrder> findPage(int page, int size) {
        Page<SeckillOrder> mypage = new Page<>(page, size);
        IPage<SeckillOrder> iPage = this.page(mypage, new QueryWrapper<SeckillOrder>());

        return new PageResult<SeckillOrder>(iPage.getTotal(), iPage.getRecords());
    }

    /**
     * SeckillOrder条件查询
     *
     * @param seckillOrder
     * @return
     */
    @Override
    public List<SeckillOrder> findList(SeckillOrder seckillOrder) {
        //构建查询条件
        QueryWrapper<SeckillOrder> queryWrapper = this.createQueryWrapper(seckillOrder);
        //根据构建的条件查询数据
        return this.list(queryWrapper);
    }


    /**
     * SeckillOrder构建查询对象
     *
     * @param seckillOrder
     * @return
     */
    public QueryWrapper<SeckillOrder> createQueryWrapper(SeckillOrder seckillOrder) {
        QueryWrapper<SeckillOrder> queryWrapper = new QueryWrapper<>();
        if (seckillOrder != null) {
            // 主键
            if (!StringUtils.isEmpty(seckillOrder.getId())) {
                queryWrapper.eq("id", seckillOrder.getId());
            }
            // 秒杀商品ID
            if (!StringUtils.isEmpty(seckillOrder.getSeckillId())) {
                queryWrapper.eq("seckill_id", seckillOrder.getSeckillId());
            }
            // 支付金额
            if (!StringUtils.isEmpty(seckillOrder.getMoney())) {
                queryWrapper.eq("money", seckillOrder.getMoney());
            }
            // 用户
            if (!StringUtils.isEmpty(seckillOrder.getUserId())) {
                queryWrapper.eq("user_id", seckillOrder.getUserId());
            }
            // 商家
            if (!StringUtils.isEmpty(seckillOrder.getSellerId())) {
                queryWrapper.eq("seller_id", seckillOrder.getSellerId());
            }
            // 创建时间
            if (!StringUtils.isEmpty(seckillOrder.getCreateTime())) {
                queryWrapper.eq("create_time", seckillOrder.getCreateTime());
            }
            // 支付时间
            if (!StringUtils.isEmpty(seckillOrder.getPayTime())) {
                queryWrapper.eq("pay_time", seckillOrder.getPayTime());
            }
            // 状态
            if (!StringUtils.isEmpty(seckillOrder.getStatus())) {
                queryWrapper.eq("status", seckillOrder.getStatus());
            }
            // 收货人地址
            if (!StringUtils.isEmpty(seckillOrder.getReceiverAddress())) {
                queryWrapper.eq("receiver_address", seckillOrder.getReceiverAddress());
            }
            // 收货人电话
            if (!StringUtils.isEmpty(seckillOrder.getReceiverMobile())) {
                queryWrapper.eq("receiver_mobile", seckillOrder.getReceiverMobile());
            }
            // 收货人
            if (!StringUtils.isEmpty(seckillOrder.getReceiver())) {
                queryWrapper.eq("receiver", seckillOrder.getReceiver());
            }
            // 交易流水
            if (!StringUtils.isEmpty(seckillOrder.getTransactionId())) {
                queryWrapper.eq("transaction_id", seckillOrder.getTransactionId());
            }
        }
        return queryWrapper;
    }

    /**
     * 删除
     *
     * @param id
     */
    @Override
    public void delete(Long id) {
        this.removeById(id);
    }

    /**
     * 修改SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void update(SeckillOrder seckillOrder) {
        this.updateById(seckillOrder);
    }

    /**
     * 增加SeckillOrder
     *
     * @param seckillOrder
     */
    @Override
    public void add(SeckillOrder seckillOrder) {
        this.save(seckillOrder);
    }

    /**
     * 根据ID查询SeckillOrder
     *
     * @param id
     * @return
     */
    @Override
    public SeckillOrder findById(Long id) {
        return this.getById(id);
    }

    /**
     * 查询SeckillOrder全部数据
     *
     * @return
     */
    @Override
    public List<SeckillOrder> findAll() {
        return this.list(new QueryWrapper<SeckillOrder>());
    }

    /*@Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;*/

    @Autowired
    private MultiThreadingCreateOrder multiThreadingCreateOrder;

    @Autowired
    private RedisTemplate redisTemplate;

    /****
     * 添加订单，添第二次，如果用户访问多，说明已经在排队
     * @param id
     * @param time
     * @param username
     */
    @Override
    public Boolean add(Long id, String time, String username) {

        //设置自增值，递增，判断是否排队，防止秒杀重复排队
        Long userQueueCount = redisTemplate.boundHashOps("UserQueueCount").increment(username, 1);
        if(userQueueCount>1){
            //100：表示有重复抢单
            throw new RuntimeException(String.valueOf(StatusCode.REPERROR));
        }

        //排队信息封装，将抢购信息封装
        SeckillStatus seckillStatus = new SeckillStatus(username, new Date(), 1, id, time);

        //将秒杀抢单信息存入到Redis中,这里采用List方式存储,List本身是一个队列
        redisTemplate.boundListOps("SeckillOrderQueue").leftPush(seckillStatus);

        //将抢单状态存入到Redis中
        // 虽然上一条代码已经将seckillStatus存放到了redis中, 但因队列"SeckillOrderQueue"存放的数据需要保留
        // 之前的顺序(记录了用户抢单顺序), 且读取时pop弹栈会删除掉数据, 所以针对订单状态的增删改查不应该在队列
        // 完成, 我们新创建一个数据库结构
        redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);

        //多线程异步下单
        multiThreadingCreateOrder.createOrder();
       /* //获取商品数据
        SeckillGoods goods = (SeckillGoods) redisTemplate.boundHashOps("SeckillGoods_" + time).get(id);

        //如果没有库存，则直接抛出异常
        if (goods == null || goods.getStockCount() <= 0) {
            throw new RuntimeException("已售罄!");
        }
        //如果有库存，则创建秒杀商品订单
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(id);
        seckillOrder.setMoney(goods.getCostPrice());
        seckillOrder.setUserId(username);
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0"); //0表示未付款，1表示已付款

        //将秒杀订单存入到Redis中
        redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);

        //库存减少
        goods.setStockCount(goods.getStockCount() - 1);

        //判断当前商品是否还有库存
        if (goods.getStockCount() <= 0) {
            //并且将商品数据同步到MySQL中
            seckillGoodsMapper.updateById(goods);
            // seckillGoodsMapper.updateByPrimaryKeySelective(goods);
            //如果没有库存,则清空Redis缓存中该商品
            redisTemplate.boundHashOps("SeckillGoods_" + time).delete(id);
        } else {
            //如果有库存，则直数据重置到Reids中
            redisTemplate.boundHashOps("SeckillGoods_" + time).put(id, goods);
        }*/
        return true;
    }

    /***
     * 抢单状态查询
     * @param username
     * @return
     */
    @Override
    public SeckillStatus queryStatus(String username) {
        return (SeckillStatus) redisTemplate.boundHashOps("UserQueueStatus").get(username);
    }
}
