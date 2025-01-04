package com.thespeace.kafkahandson.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thespeace.kafkahandson.model.MyMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.thespeace.kafkahandson.model.Topic.MY_JSON_TOPIC;

@Component
public class MyThirdConsumer {

    @KafkaListener(
            topics = { MY_JSON_TOPIC },
            groupId = "batch-test-consumer-group",
            containerFactory = "batchKafkaListenerContainerFactory"
    )
    public void accept(List<ConsumerRecord<String, String>> messages) {
        ObjectMapper objectMapper = new ObjectMapper();
        messages.forEach(message -> {
            MyMessage myMessage;
            try {
                myMessage = objectMapper.readValue(message.value(), MyMessage.class); // batchConsumer의 String 값을 직접 Object 매핑
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            System.out.println("ㄴ [Third Consumer] Value " + myMessage + " / Offset - "+ message.offset() + ", partition -" + message.partition());
        });
    }
}
