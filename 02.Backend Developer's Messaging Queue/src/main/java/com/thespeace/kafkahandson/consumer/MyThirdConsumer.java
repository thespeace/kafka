package com.thespeace.kafkahandson.consumer;

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
    public void accept(List<ConsumerRecord<String, MyMessage>> messages) {
        System.out.println("[Third Consumer] Messages arrived! - count " + messages.size());
        messages.forEach(message -> System.out.println("ㄴ [Third Consumer] Value " + message.value() + " / Offset - "+ message.offset() + ", partition -" + message.partition()));
    }
}
