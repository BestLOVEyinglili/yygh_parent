package com.atguigu.yygh.mq.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitService {

    @Autowired
    RabbitTemplate rabbitTemplate;

    /**
     *
     * @param exchange  交换机名字
     * @param routingKey  发送消息时指定的key
     * @param message  消息对象
     */
    public void sendMessage(String exchange, String routingKey, Object message){
        rabbitTemplate.convertAndSend(exchange,routingKey,message);
    }

}
