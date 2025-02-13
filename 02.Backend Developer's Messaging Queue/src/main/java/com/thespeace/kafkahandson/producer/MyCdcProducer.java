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
                String.valueOf(message.getId()), // Message Key는 서비스 요구사항에 따라 다르지만 보통 순서보장을 위해 PK를 많이 사용한다.
                objectMapper.writeValueAsString(message)
        );
    }
}
