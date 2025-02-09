package com.heima.kafka.sample.controller;

import com.heima.kafka.sample.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.fastjson.JSON;

@RestController
public class HelloController {
    @Autowired
    KafkaTemplate kafkaTemplate;

    @GetMapping("/hello")
    public String hello() {
//        kafkaTemplate.send("itcast-topic", "hello heima");
        User user = new User();
        user.setAge(18);
        user.setName("宇哥");
        kafkaTemplate.send("user-topic", JSON.toJSONString(user));
        return "ok";
    }
}
