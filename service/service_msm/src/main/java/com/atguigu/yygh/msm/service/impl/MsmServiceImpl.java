package com.atguigu.yygh.msm.service.impl;

import com.atguigu.yygh.msm.HttpUtils;
import com.atguigu.yygh.msm.service.MsmService;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MsmServiceImpl implements MsmService {

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Override
    public void sendCode(String phone) {

        //1、从redis中获取，如果有直接返回
        String code_redis = stringRedisTemplate.boundValueOps(phone).get();
        if(!StringUtils.isEmpty(code_redis)){
            return;
        }

        //2、发送验证码
        String host = "http://dingxin.market.alicloudapi.com";
        String path = "/dx/sendSms";
        String method = "POST";
        String appcode = "f28c4545f88d47e59eb306461b8cbccb";

        Map<String, String> headers = new HashMap<String, String>();
        headers.put("Authorization", "APPCODE " + appcode);
        Map<String, String> querys = new HashMap<String, String>();
        querys.put("mobile", phone);

        String code = UUID.randomUUID().toString().replaceAll("-","").substring(0,5);//随机短信验证码

        querys.put("param", "code:" +  code);
        querys.put("tpl_id", "TP1711063");//短信模板id
        Map<String, String> bodys = new HashMap<String, String>();

        try {
            HttpResponse response = HttpUtils.doPost(host, path, method, headers, querys, bodys);
            StatusLine statusLine = response.getStatusLine();//响应状态行
            int statusCode = statusLine.getStatusCode();

            //3、发送成功，验证码存入到redis
            if(statusCode==200){
                //发送成功
                //存储redis
                stringRedisTemplate.boundValueOps(phone).set(code,5, TimeUnit.MINUTES);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println(UUID.randomUUID().toString().replaceAll("-","").substring(0,5));
    }

}
