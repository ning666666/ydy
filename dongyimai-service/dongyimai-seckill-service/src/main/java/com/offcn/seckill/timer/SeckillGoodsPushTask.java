package com.offcn.seckill.timer;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.offcn.seckill.dao.SeckillGoodsMapper;
import com.offcn.seckill.pojo.SeckillGoods;
import com.offcn.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Set;

@Component //交给spring定时执行
public class SeckillGoodsPushTask {

    /****
     * cron 表达式 七子表达式
     * s m h d m week year
     * 5/15 表示从第5秒开始每15秒执行一次
     * 5,15 表示第5秒第15秒触发一次
     * /递增触发  ，指定多个值  *所有值 ?不指定值 -区间 Llast Wwork  #序号
     * 0 15 10 L * ？ 表示每个月的最后一天的上午10点15分
     * 0 15 10 ？ * 6#3 表示每个月第三周的第5天的上午10点15分
     * 每30秒执行一次
     */
    /*@Scheduled(cron = "0/30 * * * * ?") //配置定时任务执行时间
    public void loadGoodsPushRedis(){
        System.out.println("task demo");
    }*/
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private SeckillGoodsMapper seckillGoodsMapper;

    /****
     * 每30秒执行一次
     */
    @Scheduled(cron = "0/30 * * * * ?")
    public void loadGoodsPushRedis() {
        //获得时间段集合
        List<Date> dateMenus = DateUtil.getDateMenus();
        //循环时间段集合 - 从0点起算,每2小时一个时段,一天可分12个时段,本次客户要求只显示5个时段的活动商品(包含正在秒杀和即将开始的商品),故以当前时间为参照,寻找5个活动时段.
        //比如:当前时间为15点, dateMenus值为{15, 17, 19, 21, 23}
        //注:15点进入活动页面的客户也应该能看到14~16这个时段秒杀的商品,即正在秒杀的商品
        for (Date startTime : dateMenus) { // 寻找符合每个活动时段的商品, 存入redis以展示在页面

            //提取开始时间，转换为年月日时格式的字符串
            String extName = DateUtil.date2Str(startTime);
            System.out.println("extName = " + extName);
            //创建查询条件对象
            QueryWrapper<SeckillGoods> queryWrapper = new QueryWrapper<>();
            //设置查询条件 1)商品必须审核通过  status=1
            queryWrapper.eq("status", "1");
            //2)库存大于0
            queryWrapper.gt("stock_count", 0);
            //3)数据库中商品秒杀开始时间 >= 本轮活动时段的开始时间
            queryWrapper.ge("start_time", DateUtil.date2StrFull(startTime));
            //4)数据库中商品秒杀结束时间 < 本轮活动时段的开始时间+2小时, 即活动的结束时间
            queryWrapper.lt("end_time", DateUtil.date2StrFull(DateUtil.addDateHour(startTime, 2)));
            //5)判断redis中是否已缓存过此商品:
            //若没缓存则添加缓存,并设置2小时后自动删除,因为整个秒杀时段就2小时
            //若缓存过就不应再覆盖缓存,否则会盖掉库存等信息,导致秒杀扣减的库存又被初始化了回来
            //读取redis中的秒杀商品
            Set keys = redisTemplate.boundHashOps("SeckillGoods_" + extName).keys();//SeckillGoods_2022081100
            //判断keys不为空，就设置排除条件
            if (keys != null && keys.size() > 0) {
                queryWrapper.notIn("id", keys);
            }
            //查询符合条件的数据库
            List<SeckillGoods> seckillGoodsList = seckillGoodsMapper.selectList(queryWrapper);
            System.out.println("符合条件的数据: " + seckillGoodsList);
            //遍历查询到数据集合,存储数据到redis
            for (SeckillGoods seckillGoods : seckillGoodsList) {
                redisTemplate.boundHashOps("SeckillGoods_" + extName).put(seckillGoods.getId(), seckillGoods);
                //商品数据队列存储,防止高并发超卖，数组ids为库存长度个数的商品id数
                Long[] ids = pushIds(seckillGoods.getStockCount(), seckillGoods.getId());
                redisTemplate.boundListOps("SeckillGoodsCountList_" + seckillGoods.getId()).leftPushAll(ids);
                //自增计数器 - 防止redis内存中修改数据造成库存余数混乱，只能自增到库存量的位置
                redisTemplate.boundHashOps("SeckillGoodsCount").increment(seckillGoods.getId(), seckillGoods.getStockCount());
                //设置超时时间2小时
                redisTemplate.expireAt("SeckillGoods_" + extName, DateUtil.addDateHour(startTime, 2));
            }
        }
    }

    /***
     * 将商品ID存入到数组中，根据库存量创建一个数组, 数组元素的值全部相同,都是当前商品的id.
     * 用户每次使用多线程异步下单，从这个队列中也取一次值，能取到说明还有库存，防止超卖
     *
     * @param len:长度
     * @param id     :值
     * @return
     */
    public Long[] pushIds(int len, Long id) {
        Long[] ids = new Long[len];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = id;
        }
        return ids;
    }
}
