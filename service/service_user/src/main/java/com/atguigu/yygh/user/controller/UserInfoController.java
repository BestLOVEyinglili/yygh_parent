package com.atguigu.yygh.user.controller;


import com.atguigu.common.result.R;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserInfoController {

    @Autowired
    private UserInfoService userInfoService;

    @PostMapping("login")
    public R login(@RequestBody LoginVo loginVo) {
        //返回name+token
        Map<String, Object> info = userInfoService.login(loginVo);
        return R.ok().data(info);
    }
}
