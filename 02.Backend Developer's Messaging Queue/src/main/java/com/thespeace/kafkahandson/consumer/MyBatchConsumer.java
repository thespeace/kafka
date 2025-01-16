package com.thespeace.kafkahandson.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thespeace.kafkahandson.model.MyMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.thespeace.kafkahandson.model.Topic.MY_JSON_TOPIC;

@Component
public class MyBatchConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @KafkaListener(
            topics = { MY_JSON_TOPIC },
            groupId = "batch-test-consumer-group", // MyConsumer의 groupId와 반드시 달라야 함!
            containerFactory = "batchKafkaListenerContainerFactory",
            concurrency = "3" // kafkaConfig -> kafkaListener, 숫자를 늘려 처리량 개선(최대 갯수는 Leader Partition 수)
    )
    public void accept(List<ConsumerRecord<String, String>> messages, Acknowledgment acknowledgment) {
        System.out.println("[Batch Consumer] Batch message arrived! - count " + messages.size());
        messages.forEach(message -> {
            MyMessage myMessage;
            try {
                myMessage = objectMapper.readValue(message.value(), MyMessage.class); // batchConsumer의 String 값을 직접 Object 매핑
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
            System.out.println("ㄴ [Batch Consumer(" + Thread.currentThread().getId() + ")] " + "[Partition - " + message.partition() + " / Offset - " + message.offset() + "] " + myMessage);
        });

        acknowledgment.acknowledge();
    }
}
