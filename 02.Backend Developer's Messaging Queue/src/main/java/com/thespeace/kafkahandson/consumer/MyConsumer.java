package com.thespeace.kafkahandson.consumer;

import com.thespeace.kafkahandson.model.MyMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import static com.thespeace.kafkahandson.model.Topic.MY_JSON_TOPIC;

@Component
public class MyConsumer {

    @KafkaListener(
            topics = { MY_JSON_TOPIC },
            groupId = "test-consumer-group"
    )
    public void accept(ConsumerRecord<String, MyMessage> message, Acknowledgment acknowledgment) {
        System.out.println("[Main Consumer] Message arrived! - " + message.value());
        acknowledgment.acknowledge(); // consume 후에 커밋(수동)
    }
}
