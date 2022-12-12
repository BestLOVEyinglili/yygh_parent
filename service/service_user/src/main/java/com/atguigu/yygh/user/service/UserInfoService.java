package com.atguigu.yygh.user.service;

import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.vo.user.LoginVo;
import com.atguigu.yygh.vo.user.UserAuthVo;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Map;

public interface UserInfoService extends IService<UserInfo> {
    Map<String, Object> login(LoginVo loginVo);

    UserInfo getUserInfoByOpenId(String openid);

    //用户认证
    void userAuth(Long userId, UserAuthVo userAuthVo);


    void auth(Long id, Integer authStatus);

    Map<String, Object> show(Long id);

    void lock(Long id, Integer status);

    Page<UserInfo> userList(Integer page, Integer limit, UserInfoQueryVo userInfoQueryVo);
}
