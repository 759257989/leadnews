package com.heima.kafka.sample.lisenter;

import com.alibaba.fastjson.JSON;
import com.heima.kafka.sample.pojo.User;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class HelloListener {

    @KafkaListener(topics = "user-topic")
    public void onMessage(String message) {
//        if(!StringUtils.isEmpty(message)) {
//            User user = JSON.parse(message,User.class);
//            System.out.println(user);
//        }
    }
}
