package com.thespeace.kafkahandson.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.thespeace.kafkahandson.model.Topic.MY_SECOND_TOPIC;

@RequiredArgsConstructor
@Component
public class MySecondProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessageWithKey(String key, String myMessage) { // 명시적으로 key를 지정
        kafkaTemplate.send(MY_SECOND_TOPIC, key, myMessage);
    }

}
