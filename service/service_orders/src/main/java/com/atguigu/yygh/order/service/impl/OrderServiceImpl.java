package com.atguigu.yygh.order.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.handler.YyghException;
import com.atguigu.common.utils.HttpRequestHelper;
import com.atguigu.yygh.enums.OrderStatusEnum;
import com.atguigu.yygh.hosp.client.HospitalFeignClient;
import com.atguigu.yygh.model.order.OrderInfo;
import com.atguigu.yygh.model.user.Patient;
import com.atguigu.yygh.mq.service.MqConst;
import com.atguigu.yygh.mq.service.RabbitService;
import com.atguigu.yygh.order.mapper.OrderInfoMapper;
import com.atguigu.yygh.order.service.OrderService;
import com.atguigu.yygh.user.client.PatientFeignClient;
import com.atguigu.yygh.vo.hosp.ScheduleOrderVo;
import com.atguigu.yygh.vo.msm.MsmVo;
import com.atguigu.yygh.vo.order.OrderMqVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


@Service
public class OrderServiceImpl extends ServiceImpl<OrderInfoMapper, OrderInfo> implements OrderService {

    @Autowired
    HospitalFeignClient hospitalFeignClient;

    @Autowired
    PatientFeignClient patientFeignClient;

    @Autowired
    RabbitService rabbitService;


    @Override
    public Long saveOrder(String scheduleId, Long patientId) {
        //0.准备数据
        /*
        * 这里和老师不一样
        * */
        Patient patient =  patientFeignClient.getPatient(patientId);

        ScheduleOrderVo scheduleOrderVo = hospitalFeignClient.getScheduleOrderVo(scheduleId);

        //医院服务查询排班

        //1.调用医院端接口(创建医院订单,扣减医院端排班库存,返回数据)
        //医院的接口地址,从医院中设置中获取
        //service_hosp 医院服务查询设置

        String hoscode = scheduleOrderVo.getHoscode();
        String hospApiUrl = hospitalFeignClient.getHospApiUrl(hoscode);
        String url = hospApiUrl+"/order/submitOrder";

        //1.2封装参数
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("hoscode",scheduleOrderVo.getHoscode());
        paramMap.put("depcode",scheduleOrderVo.getDepcode());
        paramMap.put("hosScheduleId",scheduleOrderVo.getHosScheduleId());//医院端的排班id
         paramMap.put("reserveDate",new DateTime(scheduleOrderVo.getReserveDate()).toString("yyyy-MM-dd"));
        paramMap.put("reserveTime", scheduleOrderVo.getReserveTime());
        paramMap.put("amount",scheduleOrderVo.getAmount()); //挂号费用
        paramMap.put("name", patient.getName());
        paramMap.put("certificatesType",patient.getCertificatesType());
        paramMap.put("certificatesNo", patient.getCertificatesNo());
        paramMap.put("sex",patient.getSex());
        paramMap.put("birthdate", patient.getBirthdate());
        paramMap.put("phone",patient.getPhone());
        paramMap.put("isMarry", patient.getIsMarry());
        paramMap.put("provinceCode",patient.getProvinceCode());
        paramMap.put("cityCode", patient.getCityCode());
        paramMap.put("districtCode",patient.getDistrictCode());
        paramMap.put("address",patient.getAddress());
        paramMap.put("contactsName",patient.getContactsName());
        paramMap.put("contactsCertificatesType", patient.getContactsCertificatesType());
        paramMap.put("contactsCertificatesNo",patient.getContactsCertificatesNo());
        paramMap.put("contactsPhone",patient.getContactsPhone());
        paramMap.put("timestamp", HttpRequestHelper.getTimestamp());
        paramMap.put("sign", "");

        //1.3 使用工具类发送请求,医院端需要启动
        JSONObject jsonObject= HttpRequestHelper.sendRequest(paramMap, url);

        //1.4 医院端的返回值
        Integer code = jsonObject.getInteger("code");
        if(code!=200){
            throw new YyghException(20001,"医院端接口调用失败");
        }
        //1.5 从data中解析数据
        JSONObject data = jsonObject.getJSONObject("data");

        String hosRecordId = data.getString("hosRecordId");//医院端创建的订单的id
        Integer number = data.getInteger("number");//预约序号
        Integer reservedNumber = data.getInteger("reservedNumber");
        Integer availableNumber = data.getInteger("availableNumber");
        String fetchTime = data.getString("fetchTime");
        String fetchAddress = data.getString("fetchAddress");


        //2、创建平台端自己的订单   `yygh_order`.`order_info`  尚医通平台存储的订单
        OrderInfo orderInfo = new OrderInfo();

        orderInfo.setUserId(patient.getUserId()); // ※

        String outTradeNo = System.currentTimeMillis() + "" + new Random().nextInt(100);
        orderInfo.setOutTradeNo(outTradeNo);//订单的编号（支付和退款时都要使用），保证唯一              // ※

        orderInfo.setHoscode(scheduleOrderVo.getHoscode());
        orderInfo.setHosname(scheduleOrderVo.getHosname());
        orderInfo.setDepcode(scheduleOrderVo.getDepcode());
        orderInfo.setDepname(scheduleOrderVo.getDepname());
        orderInfo.setTitle(scheduleOrderVo.getTitle());
        orderInfo.setScheduleId(scheduleId);//平台的订单中存储平台的排班id（mg中）    // ※
        orderInfo.setReserveDate(scheduleOrderVo.getReserveDate());
        orderInfo.setReserveTime(scheduleOrderVo.getReserveTime());
        orderInfo.setPatientId(patientId);                                   // ※
        orderInfo.setPatientName(patient.getName());
        orderInfo.setPatientPhone(patient.getPhone());
        orderInfo.setHosRecordId(hosRecordId);//医院端返回的 医院端订单的id    // ※
        orderInfo.setNumber(number);//医院端返回的预约序号
        orderInfo.setFetchTime(fetchTime);
        orderInfo.setFetchAddress(fetchAddress);
        orderInfo.setAmount(scheduleOrderVo.getAmount());
        orderInfo.setQuitTime(scheduleOrderVo.getQuitTime());
        orderInfo.setOrderStatus(OrderStatusEnum.UNPAID.getStatus());//订单状态=0       // ※
        orderInfo.setCreateTime(new Date());
        orderInfo.setUpdateTime(new Date());

        baseMapper.insert(orderInfo);//主键自动返回


        //3、更新mongodb中该排班的号源数量
        //4、给就诊人发送短信通知，订单创建成功

        //3/4 -- 向第一个队列发送消息


        MsmVo msmVo = new MsmVo();
        msmVo.setPhone(patient.getPhone());//就诊人手机号
        msmVo.getParam().put("message","【尚医通】"+patient.getName()+"你好，挂号订单已创建！");//短信内容

        OrderMqVo orderMqVo = new OrderMqVo();
        orderMqVo.setScheduleId(scheduleId);//mg中排班id
        orderMqVo.setAvailableNumber(availableNumber);//医院端返回的num
        orderMqVo.setReservedNumber(reservedNumber);
        orderMqVo.setMsmVo(msmVo);//医院服务向第二个队列中发送的消息

        rabbitService.sendMessage(MqConst.EXCHANGE_DIRECT_ORDER,MqConst.ROUTING_ORDER,orderMqVo);


        return orderInfo.getId();
    }

}
