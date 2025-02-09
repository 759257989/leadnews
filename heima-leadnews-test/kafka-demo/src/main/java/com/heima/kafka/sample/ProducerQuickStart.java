package com.heima.kafka.sample;

import org.apache.kafka.clients.producer.*;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerQuickStart {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        //连接配置信息
        Properties props = new Properties();
        props.put("bootstrap.servers", "192.168.200.130:9092");
        //key value 序列化器
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
//        props.put(ProducerConfig.ACKS_CONFIG, "all"); //ack 确保所有节点收到
        props.put(ProducerConfig.RETRIES_CONFIG,10); //设置重试参数
        props.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "lz4"); //设置消息压缩算法参数
        //kafka生产者对象
        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(props);

        //发送消息
        //参数1 topic， 2 key， 3value
        ProducerRecord<String, String> producerRecord = new ProducerRecord<String,String>("topic-first", "key-001", "hello");
//        //同步发送
//        RecordMetadata recordMetadata = producer.send(producerRecord).get();
//        System.out.println(recordMetadata.offset());

        //异步发送
        producer.send(producerRecord, new Callback() {
            @Override
            public void onCompletion(RecordMetadata recordMetadata, Exception exception) {
                if (exception != null) {
                    System.out.println("异常消息");
                }
                System.out.println(recordMetadata.offset());
            }
        });


        //关闭通道
        producer.close();
    }
}
