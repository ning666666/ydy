package com.offcn.order.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.offcn.order.pojo.PayLog;

/****
 * @Author:ujiuye
 * @Description:PayLog的Dao判断如果支付方式为支付宝支付，向数据库插入支付日志记录，并放入redis存储
 * @Date 2021/2/1 14:19
 *****/
public interface PayLogMapper extends BaseMapper<PayLog> {
}
