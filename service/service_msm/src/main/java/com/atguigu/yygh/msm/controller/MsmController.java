package com.atguigu.yygh.msm.controller;


import com.atguigu.common.result.R;
import com.atguigu.yygh.msm.service.MsmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/msm")
public class MsmController {

    @Autowired
    MsmService msmService;

    //登录弹出框的获取验证码按钮时被调用
    @GetMapping(value = "/send/{phone}")
    public R code(@PathVariable String phone) {
        msmService.sendCode(phone);
        return R.ok();
    }

}
