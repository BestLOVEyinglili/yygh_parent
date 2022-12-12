package com.atguigu.yygh.user.controller;

import com.atguigu.common.result.R;
import com.atguigu.yygh.enums.AuthStatusEnum;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.util.AuthContextHolder;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RequestMapping("/api/user")
public class UserInfoApiController {


    @Autowired
    UserInfoService userInfoService;

   /* @PostMapping("login")
    public R login(@RequestBody LoginVo loginVo){
        Map<String,Object> map = userInfoService.login(loginVo);
        return R.ok().data(map);
    }*/

    @PostMapping("/auth/userAuth")
    public R userAuth(@RequestBody UserAuthVo userAuthVo, HttpServletRequest request) {
        userInfoService.userAuth(AuthContextHolder.getUserId(request),userAuthVo);
        return R.ok();
    }

    @GetMapping("/auth/getUserInfo")
    public R getUserInfo(HttpServletRequest request) {

        Long userId = AuthContextHolder.getUserId(request);
        UserInfo userInfo = userInfoService.getById(userId);

        // 设置 认证状态 0：未认证 1：认证中 2：认证成功 -1：认证失败
        String authStatusString = "";
        AuthStatusEnum[] values = AuthStatusEnum.values();
        for (int i = 0; i < values.length; i++) {
            AuthStatusEnum statusEnum = values[i];
            if(statusEnum.getStatus().intValue()==userInfo.getAuthStatus().intValue()){
                authStatusString = statusEnum.getName();
            }
        }

        userInfo.getParam().put("authStatusString",authStatusString);

        return R.ok().data("userInfo",userInfo);
    }
}
