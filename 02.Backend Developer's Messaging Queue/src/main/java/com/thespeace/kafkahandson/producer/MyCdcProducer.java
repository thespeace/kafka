package com.thespeace.kafkahandson.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thespeace.kafkahandson.common.CustomObjectMapper;
import com.thespeace.kafkahandson.model.MyCdcMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.thespeace.kafkahandson.model.Topic.MY_CDC_TOPIC;

@RequiredArgsConstructor
@Component
public class MyCdcProducer {

    CustomObjectMapper objectMapper = new CustomObjectMapper();

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendMessage(MyCdcMessage message) throws JsonProcessingException {
        kafkaTemplate.send(
                MY_CDC_TOPIC,
                objectMapper.writeValueAsString(message)
        );
    }
}
