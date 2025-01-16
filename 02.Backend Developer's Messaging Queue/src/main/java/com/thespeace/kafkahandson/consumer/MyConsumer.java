package com.thespeace.kafkahandson.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.thespeace.kafkahandson.model.MyMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.thespeace.kafkahandson.model.Topic.MY_JSON_TOPIC;

@Component
public class MyConsumer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * <p>여러 번 Comsume을 해도 한 번만 데이터 처리를 하도록 구현해보자.</p>
     * <p>redis 등 외부에서 상태를 관리하는 툴을 사용해 멱등성을 보장해주는 키 값을 관리하게 되는데, 개념적인 것만 보면 되기 때문에 Memory에서 관리하는 것으로 구현해보자.</p>
     */
    private final Map<String, Integer> idHistoryMap = new ConcurrentHashMap<>(); // 동시성 문제 대비 ConcurrentHashMap 사용

    @KafkaListener(
            topics = { MY_JSON_TOPIC },
            groupId = "test-consumer-group",
            concurrency = "1" // kafkaConfig -> kafkaListener, 숫자를 늘려 처리량 개선(최대 갯수는 Leader Partition 수)
    )
    public void listen(ConsumerRecord<String, String> message, Acknowledgment acknowledgment) throws JsonProcessingException {
        MyMessage myMessage = objectMapper.readValue(message.value(), MyMessage.class);
        this.printPayloadIfFirstMessage(myMessage);
//        Thread.sleep(1000); // 하나의 데이터를 처리할 때, 1초가 걸리도록 설정해서 처리량을 console로 확인해보자.
        acknowledgment.acknowledge();
    }

    /**
     * <h2>중복 메시지가 아닌 첫번째 메시지만 Print</h2>
     * <p>정책 : Id에 대해 Unique하게 Exactly Once를 보장</p>
     */
    private synchronized void printPayloadIfFirstMessage(MyMessage myMessage) {
        if (idHistoryMap.putIfAbsent(String.valueOf(myMessage.getId()), 1) == null) {
            System.out.println("[Main Consumer] Message arrived! - " + myMessage); // Exactly Once 실행되어야 하는 로직이라고 가정.
        } else {
            System.out.println("[Main Consumer] Duplicate! (" + myMessage.getId() + ")");
        }
    }
}
