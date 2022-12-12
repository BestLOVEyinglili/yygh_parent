package com.atguigu.yygh.mq.service;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {

    //消息转换器,可以使用自定义的类当做消息对象

    //发送消息：  XxxxMessage(自定义消息对象)---》序列化json，转成字节，利用字节构造Message对象（mq中的类）
    //接收消息： Message对象，反序列化成字节，转成json，最后还原成自定义消息对象
    @Bean
    public MessageConverter messageConverter(){
        return new Jackson2JsonMessageConverter();
    }
}