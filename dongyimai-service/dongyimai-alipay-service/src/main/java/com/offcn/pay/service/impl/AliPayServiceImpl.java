package com.offcn.pay.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePrecreateRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradePrecreateResponse;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.offcn.pay.service.AliPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by travelround on 2021/6/25.
 */
@Service
public class AliPayServiceImpl implements AliPayService {

    @Autowired
    private AlipayClient alipayClient;

    /**
     * 生成支付宝支付二维码
     *
     * @param out_trade_no 订单号
     * @param total_fee    金额(分)
     * @return
     */
    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //创建返回参数集合
        Map map = new HashMap();
        try {
            //实现预下单请求接口
            AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
            //request.setNotifyUrl("");//请求生成二维码的地址
            JSONObject bizContent = new JSONObject(); //创建请参数对象
            bizContent.put("out_trade_no", out_trade_no);
            bizContent.put("total_amount", total_fee);
            bizContent.put("subject", "测试商品");
            //封装请求参数
            request.setBizContent(bizContent.toString());
            //想阿里客户端发送请求，得到响应数据
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            //从响应中取出结果
            String code = response.getCode();
            if("10000".equals(code)){
                map.put("qrcode",response.getQrCode());
                map.put("out_trade_no",response.getOutTradeNo());
                map.put("total_fee",total_fee);
            } else {
                System.out.println("调用失败"+response.getBody());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    /**
     * 交易查询接口alipay.trade.query：
     * 获取指定订单编号的，交易状态
     * @throws AlipayApiException
     */
    @Override
    public  Map<String,String> queryPayStatus(String out_trade_no){
        Map<String,String> map=new HashMap<String, String>();
        AlipayTradeQueryResponse response = null;
        try {
            //AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key","RSA2");
            //实现交易查询接口
            AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
            //request.setNotifyUrl("");//请求生成二维码的地址
            JSONObject bizContent = new JSONObject(); //创建请参数对象
            bizContent.put("out_trade_no", out_trade_no);
            //bizContent.put("total_amount", total_fee);
            //bizContent.put("subject", "测试商品");
            //封装请求参数
            request.setBizContent(bizContent.toString());
            response = alipayClient.execute(request);
            String code = response.getCode();
            if("10000".equals(code)){
                map.put("out_trade_no",response.getOutTradeNo());//商家订单号
                map.put("tradeStatus",response.getTradeStatus());//支付状态
                map.put("trade_no",response.getTradeNo());//支付宝交易流水号
            } else {
                System.out.println("调用失败"+response.getBody());
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Value("${alipay.notify-url}")
    private String notifyUrl;

    @Override
    public Map<String, String> createNative(Map<String, String> parameters) {
        //创建阿里支付客户端请求对象
        Map<String, String> map = new HashMap<String, String>();

        //创建预下单请求对象
        AlipayTradePrecreateRequest request = new AlipayTradePrecreateRequest();
        //设置回调地址
        request.setNotifyUrl(notifyUrl);
        //设置预下单请求参数
        request.setBizContent("{" +
                "    \"out_trade_no\":\"" + parameters.get("out_trade_no") + "\"," +
                "    \"total_amount\":\"" + parameters.get("total_fee") + "\"," +
                "    \"subject\":\"测试购买商品001\"," +
                "    \"store_id\":\"xa_001\"," +
                "    \"timeout_express\":\"90m\"}");//设置业务参数
        //发出预下单业务请求
        try {
            AlipayTradePrecreateResponse response = alipayClient.execute(request);
            //从相应对象读取相应结果
            String code = response.getCode();
            System.out.println("响应码:" + code);
            //全部的响应结果
            String body = response.getBody();
            System.out.println("返回结果:" + body);

            if (code.equals("10000")) {
                map.put("qrcode", response.getQrCode());
                map.put("out_trade_no", response.getOutTradeNo());
                map.put("total_fee", parameters.get("total_fee"));
                System.out.println("qrcode:" + response.getQrCode());
                System.out.println("out_trade_no:" + response.getOutTradeNo());
                System.out.println("total_fee:" + parameters.get("total_fee"));
            } else {
                System.out.println("预下单接口调用失败:" + body);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return map;
    }

}
