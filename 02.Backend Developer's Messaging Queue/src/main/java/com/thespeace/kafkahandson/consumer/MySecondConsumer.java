package com.thespeace.kafkahandson.consumer;

import com.thespeace.kafkahandson.model.MyMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.thespeace.kafkahandson.model.Topic.MY_SECOND_TOPIC;

@Component
public class MySecondConsumer {

    @KafkaListener(
            topics = { MY_SECOND_TOPIC },
            groupId = "test-consumer-group",
            containerFactory = "secondKafkaListenerContainerFactory" // "" : 지정하지 않으면 @primary containerFactory를 사용한다.
    )
    public void accept(ConsumerRecord<String, String> message) {
        System.out.println("[Second Consumer] Message arrived! - " + message.value());
        System.out.println("[Second Consumer] Offset - " + message.offset() + ", Partition - " + message.partition()); //meta 정보를 확인 가능
    }
}