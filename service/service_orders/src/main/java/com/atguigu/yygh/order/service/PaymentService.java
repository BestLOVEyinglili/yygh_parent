package com.atguigu.yygh.order.service;

import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.baomidou.mybatisplus.extension.service.IService;

public interface PaymentService extends IService<PaymentInfo> {
    void createPayment(OrderInfo orderInfo,Integer paymentType);
    //为订单创建支付记录
    //每一个订单只能有一个支付记录
}
