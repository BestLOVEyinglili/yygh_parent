package com.atguigu.yygh.user.util;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ConstantPropertiesUtil implements InitializingBean {
    @Value("${wx.open.app_id}")
    String appid ;

    @Value("${wx.open.redirect_url}")
    String redirectUrl ;

    @Value("${wx.open.app_secret}")
    String app_secret;

    public static String  APP_ID ;
    public static String  REDIRECT_URL;
    public static String  APP_SECRET;

    //当前bean实例化完成之后（各个属性有值），调用该方法
    @Override
    public void afterPropertiesSet() throws Exception {
        APP_ID = this.appid;
        REDIRECT_URL = this.redirectUrl;
        APP_SECRET = this.app_secret;
    }
}
