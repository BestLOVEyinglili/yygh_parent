package com.atguigu.yygh.user.controller;


import com.atguigu.common.result.R;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.vo.user.UserInfoQueryVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/user")
public class UserController {

    @Autowired
    UserInfoService userInfoService;

    @GetMapping("auth/{id}/{authStatus}")
    public R auth(@PathVariable Long id, @PathVariable Integer authStatus){
        userInfoService.auth(id,authStatus);
        return R.ok();
    }


    @GetMapping("show/{id}")
    public R show(@PathVariable Long id){
        Map<String,Object> map = userInfoService.show(id);//用户信息+就诊人列表
        return R.ok().data(map);
    }

    @GetMapping("lock/{id}/{status}")
    public R lock(@PathVariable Long id,@PathVariable Integer status){
        userInfoService.lock(id,status);
        return R.ok();
    }

    @PostMapping("/userlist/{page}/{limit}")
    public R userList(@PathVariable Integer page,
                      @PathVariable Integer limit,
                      @RequestBody UserInfoQueryVo userInfoQueryVo){
        Page<UserInfo> pageResult = userInfoService.userList(page,limit,userInfoQueryVo);
        List<UserInfo> records = pageResult.getRecords();
        long total = pageResult.getTotal();
        return R.ok().data("pages",pageResult);
    }


}
