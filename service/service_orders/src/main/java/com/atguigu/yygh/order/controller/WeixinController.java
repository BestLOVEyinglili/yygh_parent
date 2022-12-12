package com.atguigu.yygh.order.controller;


import com.atguigu.common.result.R;
import com.atguigu.yygh.order.service.WeixinService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/order/weixin")
public class WeixinController {

    @Autowired
    WeixinService weixinService;

    //点击支付按钮时
    @GetMapping("/createNative/{orderId}")
    public R createNative(@PathVariable Long orderId){
        //map（codeUrl，支付链接，订单id）
        Map map = weixinService.createNative(orderId);
        return R.ok().data(map);
    }

}
