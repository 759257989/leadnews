package com.heima.kafka.sample;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

public class ConsumerQuickStart {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.200.130:9092");
        //设置消费者属于的消费组
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "group2");
        //key value 反序列化
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);  //手动提交偏移量
        //创建消费对象
        KafkaConsumer<String, String> cons = new KafkaConsumer<String, String>(props);

        // 订阅主题
        cons.subscribe(Collections.singletonList("topic-first"));
        //获取消息
        while (true) {
            ConsumerRecords<String, String> consumerRecords = cons.poll(Duration.ofMillis(1000));
            for (ConsumerRecord<String, String> record : consumerRecords) {
                System.out.println("Key: " + record.key());
                System.out.println("Value: " + record.value());


            }
        }
    }
}
