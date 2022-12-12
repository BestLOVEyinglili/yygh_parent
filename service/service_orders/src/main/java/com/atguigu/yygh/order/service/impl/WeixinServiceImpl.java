package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.enums.PaymentTypeEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.order.service.OrderInfoService;
import com.atguigu.yygh.order.service.PaymentService;
import com.atguigu.yygh.order.service.WeixinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class WeixinServiceImpl implements WeixinService {

    @Autowired
    PaymentService paymentService;

    @Autowired
    OrderInfoService orderInfoService;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public Map createNative(Long orderId) {

        //1、先看redis中是否有支付链接
        Map map = (Map) redisTemplate.boundValueOps(orderId).get();
        if(map!=null){
            return map;
        }

        //2、为订单创建支付记录
        OrderInfo orderInfo = orderInfoService.getById(orderId);
        paymentService.createPayment(orderInfo, PaymentTypeEnum.WEIXIN.getStatus());

        //3、调用微信端的“统一下单接口”，获取codeUrl

        return null;
    }

}
