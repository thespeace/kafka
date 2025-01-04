package com.thespeace.kafkahandson.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thespeace.kafkahandson.model.MyMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.thespeace.kafkahandson.model.Topic.MY_JSON_TOPIC;

@RequiredArgsConstructor
@Component
public class MyProducer {

    ObjectMapper objectMapper = new ObjectMapper();
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(MyMessage myMessage) throws JsonProcessingException {
        kafkaTemplate.send(
            MY_JSON_TOPIC,
            String.valueOf(myMessage.getAge()),
            objectMapper.writeValueAsString(myMessage)
        );
    }

}
