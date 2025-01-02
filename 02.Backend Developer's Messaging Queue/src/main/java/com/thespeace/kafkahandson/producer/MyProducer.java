package com.thespeace.kafkahandson.producer;

import com.thespeace.kafkahandson.model.MyMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.thespeace.kafkahandson.model.Topic.MY_JSON_TOPIC;

@RequiredArgsConstructor
@Component
public class MyProducer {

    private final KafkaTemplate<String, MyMessage> kafkaTemplate;

    public void sendMessage(MyMessage myMessage) {
        kafkaTemplate.send(MY_JSON_TOPIC, String.valueOf(myMessage.getAge()), myMessage);
    }

}
