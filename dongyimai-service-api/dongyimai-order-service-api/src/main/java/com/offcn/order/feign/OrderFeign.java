package com.offcn.order.feign;

import com.offcn.entity.Result;
import com.offcn.order.pojo.PayLog;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by travelround on 2021/6/23.
 */
@FeignClient(name = "order")
public interface OrderFeign {
    /**
     查询用户的支付日志
     @return
     */
    @GetMapping("/order/searchPayLogFromRedis")
    public Result<PayLog> searchPayLogFromRedis();

    /**
     * 修改订单状态
     * @param out_trade_no
     * @param transaction_id
     * @return
     */
    @RequestMapping(value = "/order/updateOrderStatus",method = RequestMethod.GET)
    public Result updateOrderStatus(
            @RequestParam(value="out_trade_no")  String out_trade_no,
            @RequestParam(value="transaction_id") String transaction_id);
}
