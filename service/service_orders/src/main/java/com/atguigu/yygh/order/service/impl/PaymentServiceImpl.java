package com.atguigu.yygh.order.service.impl;

import com.atguigu.yygh.enums.PaymentStatusEnum;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.order.PaymentInfo;
import com.atguigu.yygh.order.mapper.PaymentMapper;
import com.atguigu.yygh.order.service.PaymentService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, PaymentInfo> implements PaymentService {

    @Override
    public void createPayment(OrderInfo orderInfo,Integer paymentType) {

        //判断当前订单的支付记录是否存在
        //根据 order_id/out_trade_no 查询支付记录
        //payment_type

        QueryWrapper<PaymentInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("order_id",orderInfo.getId());
        queryWrapper.eq("payment_type",paymentType);
        Integer integer = baseMapper.selectCount(queryWrapper);
        if(integer>0){
            return;
        }

        PaymentInfo paymentInfo = new PaymentInfo();

        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderInfo.getId());
        paymentInfo.setPaymentType(paymentType);

//        paymentInfo.setTradeNo();支付完成后的微信端返回的流水号
//        paymentInfo.setCallbackTime(); 支付完成的时间
//        paymentInfo.setCallbackContent(); 支付完成后，微信端返回的所有数据

        paymentInfo.setTotalAmount(orderInfo.getAmount());

        Date reserveDate = orderInfo.getReserveDate();
        String date = new DateTime(reserveDate).toString("yyyy-MM-dd");
        String patientName = orderInfo.getPatientName();
        String hosname = orderInfo.getHosname();
        String depname = orderInfo.getDepname();
        String title = orderInfo.getTitle();

        paymentInfo.setSubject(date+patientName+hosname+depname+title);//交易内容   yyyy-MM-dd|张三|协和医院|心内科|教授
        paymentInfo.setPaymentStatus(PaymentStatusEnum.UNPAID.getStatus());//支付中（未支付）

        baseMapper.insert(paymentInfo);

    }
}
