package com.atguigu.yygh.user.controller;


import com.alibaba.fastjson.JSONObject;
import com.atguigu.common.result.R;
import com.atguigu.common.utils.JwtHelper;
import com.atguigu.yygh.model.user.UserInfo;
import com.atguigu.yygh.user.service.UserInfoService;
import com.atguigu.yygh.user.util.ConstantPropertiesUtil;
import com.atguigu.yygh.user.util.HttpClientUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;



/*
* 描述:获取参数
* */
@Controller
@RequestMapping("/api/user/wx")
public class WeixinApiController {

    @Autowired
    UserInfoService userInfoService;


    /*
    * 描述:微信端扫描二维码，点击允许，就会调用该接口。
    * */
    @GetMapping("callback")
    public String callback(String code){

        //1、微信端确认后，回调该接口，获取临时票据 code
        System.out.println("code="+code);

        //2.根据临时票据code获取access_token和openid
        String  url = "https://api.weixin.qq.com/sns/oauth2/access_token?" +
                "appid="+ConstantPropertiesUtil.APP_ID+
                "&secret="+ConstantPropertiesUtil.APP_SECRET+
                "&code="+code+
                "&grant_type=authorization_code";

        //3.发请求,调用该接口

        try {

            //{"access_token":"63_u6ZgOnKoTcB7IH5nwUoP-wf3cr7P_H87RK5Vy176ez7OOXupgSYzDbp_LNCStPW7AJYpRoYZzrfMzWtvM0sqFO-iZn4Ho0Fk2fm-B8Ze5Vo","expires_in":7200,"refresh_token":"63_wAfw-yYD9F4RAEiHgFAaa5Abl6L-MY7JRtNMHRZXAazApkYgySfxUy10_6kvXzDycDAa291--iYfnGAjZQaqZRYqNY2lFjfuYpLb7oAJzlM","openid":"o3_SC577mZf1Q-E_ophDGFgxImCo","scope":"snsapi_login","unionid":"oWgGz1DqP45rRoWhIekFyQsmDG80"}
            String s = HttpClientUtils.get(url);
            JSONObject jsonObject = JSONObject.parseObject(s);

            String openid = jsonObject.getString("openid");
            String access_token = jsonObject.getString("access_token");

            //2、从`yygh_user`.`user_info` 查询微信用户是否存在
            UserInfo userInfo = userInfoService.getUserInfoByOpenId(openid);

            //微信用户的自动注册
            if(userInfo==null){

                //查询微信昵称
                String url_userinfo = "https://api.weixin.qq.com/sns/userinfo?" +
                        "access_token=" + access_token +
                        "&openid="+openid;

                String s1 = HttpClientUtils.get(url_userinfo);
                JSONObject jsonObject_userinfo = JSONObject.parseObject(s1);
                String nickname = jsonObject_userinfo.getString("nickname");

                userInfo = new UserInfo();
                userInfo.setOpenid(openid);
                userInfo.setNickName(nickname);//微信昵称
                userInfoService.save(userInfo);
            }

            //参数1：name=右上角显示的
            String name = userInfo.getName();
            if(StringUtils.isEmpty(name)){
                name = userInfo.getNickName();
                if(StringUtils.isEmpty(name)){
                    name = userInfo.getPhone();
                }
            }
            //参数2：jwt令牌
            String token = JwtHelper.createToken(userInfo.getId(),name);

            //参数3：openid，如果当前用户的手机号为空（需要绑定手机号），传递openid
            return "redirect:http://localhost:3000/weixin/callback?" +
                    "name="+URLEncoder.encode(name,"utf-8")  +"&" +
                    "token="+token + "&" +
                    "openid="+  (StringUtils.isEmpty(userInfo.getPhone())==true?userInfo.getOpenid():"") ;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    @ResponseBody
    @GetMapping("getLoginParam")
    public R getLoginParam() throws UnsupportedEncodingException {
        HashMap<String, Object> map = new HashMap<>();
        map.put("appid", ConstantPropertiesUtil.APP_ID);
        map.put("scope","snsapi_login");
        map.put("redirectUri", URLEncoder.encode(ConstantPropertiesUtil.REDIRECT_URL, "utf-8"));
        map.put("state",System.currentTimeMillis());

        return R.ok().data(map);
    }
}
