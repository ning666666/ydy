package com.offcn.pay.controller;

import com.offcn.entity.Result;
import com.offcn.entity.StatusCode;
import com.offcn.order.feign.OrderFeign;
import com.offcn.order.pojo.PayLog;
import com.offcn.pay.service.AliPayService;
import com.offcn.utils.IdWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by travelround on 2021/6/25.
 */
@RestController
@RequestMapping("/pay")
public class PayController {


    @Autowired
    private AliPayService aliPayService;

    @Autowired
    private OrderFeign orderFeign;

    /**
     * 生成二维码
     *
     * @return
     */
    @GetMapping("/createNative")
    public Map createNative() {
        //IdWorker idworker = new IdWorker();
        //return aliPayService.createNative(idworker.nextId() + "", "1"); //订单id

        //用户通过oauth认证授权登录的用户名在订单searchPayLogFromRedis已经封装，直接调就可以
        //把元转化为分
        Result<PayLog> payLogResult = orderFeign.searchPayLogFromRedis();
        PayLog payLog = payLogResult.getData();
        Long totalFee = payLog.getTotalFee();
        BigDecimal bigDecimal = new BigDecimal(totalFee);
        BigDecimal divide = bigDecimal.divide(BigDecimal.valueOf(100));
        if (payLog != null) {
            return aliPayService.createNative(payLog.getOutTradeNo(), divide + "");
        } else {
            return new HashMap();
        }
    }

    /**
     * 查询支付状态
     *
     * @param out_trade_no
     * @return
     */
    @RequestMapping("/queryPayStatus")
    public Result queryPayStatus(String out_trade_no) {
        Result result = null;
        int x = 0;
        while (true) {
            //调用交易查询接口
            Map<String, String> map = null;
            try {
                map = aliPayService.queryPayStatus(out_trade_no);
            } catch (Exception e1) {
                e1.printStackTrace();
                System.out.println("调用查询服务出错");
            }
            if (map == null) {//出错
                result = new Result(false, StatusCode.ERROR, "支付出错");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_SUCCESS")) {//如果成功
                result = new Result(true, StatusCode.OK, "支付成功");
                orderFeign.updateOrderStatus(map.get("out_trade_no"), map.get("trade_no"));
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_CLOSED")) {//如果成功
                result = new Result(true, StatusCode.OK, "未付款交易超时关闭，或支付完成后全额退款");
                break;
            }
            if (map.get("tradestatus") != null && map.get("tradestatus").equals("TRADE_FINISHED")) {//如果成功
                result = new Result(true, StatusCode.OK, "交易结束，不可退款");
                break;
            }
            try {
                Thread.sleep(3000);//上上面死循环，间隔三秒一次
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            x++;
            if (x >= 100) { //30秒失效
                result = new Result(false, StatusCode.ERROR, "二维码超时");
                break;
            }
        }
        return result;
    }

    @RequestMapping("/create/native")
    public Result<Map> createNative(@RequestParam Map<String, String> parameters) {
        //获取用户名

        Map<String, String> resultMap = aliPayService.createNative(parameters);

        return new Result<Map>(true, StatusCode.OK, "二维码连接地址创建成功啦！！！", resultMap);
    }
}
