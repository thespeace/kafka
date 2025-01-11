package com.thespeace.kafkahandson.consumer;

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

    /**
     * <p>여러 번 Comsume을 해도 한 번만 데이터 처리를 하도록 구현해보자.</p>
     * <p>redis 등 외부에서 상태를 관리하는 툴을 사용해 멱등성을 보장해주는 키 값을 관리하게 되는데, 개념적인 것만 보면 되기 때문에 Memory에서 관리하는 것으로 구현해보자.</p>
     */
    private final Map<String, Integer> idHistoryMap = new ConcurrentHashMap<>(); // 동시성 문제 대비 ConcurrentHashMap 사용

    @KafkaListener(
            topics = { MY_JSON_TOPIC },
            groupId = "test-consumer-group"
    )
    public void accept(ConsumerRecord<String, MyMessage> message, Acknowledgment acknowledgment) {
        printPayloadIfFirstMessage(message.value());
        acknowledgment.acknowledge(); // consume 후에 커밋(수동)
    }

    /**
     * <h2>중복 메시지가 아닌 첫번째 메시지만 Print</h2>
     * <p>정책 : Id에 대해 Unique하게 Exactly Once를 보장</p>
     */
    private synchronized void printPayloadIfFirstMessage(MyMessage myMessage) {
        if(idHistoryMap.get(String.valueOf(myMessage.getId())) == null) {
            System.out.println("[Main Consumer] Message arrived! - " + myMessage); // Exactly Once 실행되어야 하는 로직이라고 가정.
            idHistoryMap.put(String.valueOf(myMessage.getId()), 1);
        } else {
            System.out.println("[Main Consumer] Duplicate! (" + myMessage.getId() + ")");
        }
    }
}
