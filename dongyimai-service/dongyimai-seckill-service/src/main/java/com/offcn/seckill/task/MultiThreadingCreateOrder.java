package com.offcn.seckill.task;

import com.offcn.seckill.dao.SeckillGoodsMapper;
import com.offcn.seckill.entity.SeckillStatus;
import com.offcn.seckill.pojo.SeckillGoods;
import com.offcn.seckill.pojo.SeckillOrder;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class MultiThreadingCreateOrder {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    @Autowired
    private IdWorker idWorker;

    /***
     * 多线程下单操作，这里完整的是实现了多线程抢单（进程可以是单线程，但异步必须是多线程，主任务去完成当前应用的操作了）
     * 首先开启spring异步，结合多线程下单（显示抢购成功，但下单操作是异步进行的），实现多线程异步下单，但是异
     * 步不方便管理时序，也就是做不能保证先抢购的人一定会先下单，所以还需要用redis队列，用户选择下单时，我们将
     * 抢购信息封装进实体类，并将封装的抢购数据从左边放入队列，多线程下单时从右边取出抢购数据，保证有序，完成排队，
     * 一起实现抢单，先抢到的人先下单。
     * 实现超卖：创建库存长度个数的商品id数组，并将数组放入队列，每次多线程异步下单，还会取一次商品id队列，
     * 有说明还有库存，没有就取消排队和下单，同时设置库存余数的自增计数器，防止redis在内存中修改数据，导致库存被修改；
     *
     * 创建redis队列使用list数据类型，List本身是一个队列，
     * 还有基于List的 LPUSH+BRPOP 的实现、基于Sorted-Set的实现、PUB/SUB（订阅/发布模式），steam
     */
    @Async
    public void createOrder() {
       /* try {
            System.out.println("准备执行....");
            Thread.sleep(20000);
            System.out.println("开始执行....");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/

        //从队列中获取排队信息(封装了抢购信息)
        SeckillStatus seckillStatus = (SeckillStatus) redisTemplate.boundListOps("SeckillOrderQueue").rightPop();
        try {
          /*  //时间区间
            String time = "2021062616";
            //用户登录名
            String username = "测试用户";
            //用户抢购商品
            Long id = 1L;*/

            //从队列中获取一个商品
            Object sgood = redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillStatus.getGoodsId()).rightPop();
            if (sgood == null) {
                //清理当前用户的排队信息
                clearQueue(seckillStatus);
                return;
            }

            if (seckillStatus != null) {
                //时间区间
                String time = seckillStatus.getTime();
                //用户登录名
                String username = seckillStatus.getUsername();
                //用户抢购商品
                Long id = seckillStatus.getGoodsId();
                //获取商品数据
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
                seckillOrder.setStatus("0");

                //将秒杀订单存入到Redis中
                redisTemplate.boundHashOps("SeckillOrder").put(username, seckillOrder);

               /* //库存减少
                goods.setStockCount(goods.getStockCount() - 1);

                //判断当前商品是否还有库存
                if (goods.getStockCount() <= 0) {*/
                //商品库存-1
                Long surplusCount = redisTemplate.boundHashOps("SeckillGoodsCount").increment(id, -1);//商品数量递减
                goods.setStockCount(surplusCount.intValue());    //根据计数器统计，重新设置库存

                //判断当前商品是否还有库存
                if (surplusCount <= 0) {
                    //并且将商品数据同步到MySQL中
                    seckillGoodsMapper.updateById(goods);
                    //如果没有库存,则清空Redis缓存中该商品
                    redisTemplate.boundHashOps("SeckillGoods_" + time).delete(id);
                } else {
                    //如果有库存，则直数据重置到Reids中
                    redisTemplate.boundHashOps("SeckillGoods_" + time).put(id, goods);
                }
                //抢单成功，更新抢单状态,排队->等待支付
                seckillStatus.setStatus(2);
                seckillStatus.setOrderId(seckillOrder.getId());
                seckillStatus.setMoney(Float.parseFloat(seckillOrder.getMoney()));
                redisTemplate.boundHashOps("UserQueueStatus").put(username, seckillStatus);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /***
     * 清理用户排队信息
     *
     * @param seckillStatus
     */
    private void clearQueue(SeckillStatus seckillStatus) {
        //清理排队标示
        redisTemplate.boundHashOps("UserQueueCount").delete(seckillStatus.getUsername());

        //清理抢单标示
        redisTemplate.boundHashOps("UserQueueStatus").delete(seckillStatus.getUsername());
    }

}