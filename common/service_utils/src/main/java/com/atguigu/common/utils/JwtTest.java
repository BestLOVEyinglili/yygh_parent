package com.atguigu.common.utils;

public class JwtTest {

    public static void main1(String[] args) {
        String token = JwtHelper.createToken(123L, "tom");
        System.out.println(token);
    }

    public static void main(String[] args) {
        //从jwt令牌中解析用户信息，如果解析时如果报错了，说明令牌是错的、
        String token = "eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAKtWKi5NUrJSiox099ANDXYNUtJRSq0oULIyNDM3MDayNLUw01EqLU4t8kwBihkZQzh-ibmpQE0l-blKtQCnsBlXQgAAAA.N820haRnb8ZOS-3dz39TxKVMNw2ip5l6wq7NhnMg7A1ycEPh8ylYfqXteYmGUa95XPDJDKA-GG-2Ukz07WTlaw";

        Long userId = JwtHelper.getUserId(token);
        String userName = JwtHelper.getUserName(token);

        System.out.println(userId);
        System.out.println(userName);
    }

}
