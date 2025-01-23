package com.thespeace.kafkahandson.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thespeace.kafkahandson.model.MyModelConverter;
import com.thespeace.kafkahandson.producer.MyCdcProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * <h1>Application event listener</h1>
 * <p>서비스단에서 Produce를 직접 발행하지 않고, 내부 이벤트로 발행하여 이벤트 퍼블리시된 것을 해당 이벤트 리스너가 받아서 Produce한다.</p>
 */
@RequiredArgsConstructor
@Component
public class MyCdcApplicationEventListener {

    private  final MyCdcProducer myCdcProducer;

    /**
     * <p>DB Commit이 완료된 후 해당 이벤트 실행 후 Produce</p>
     * <p>Spring 내부 이벤트인 매개변수 event를 받아 외부로 가는 Kafka event 매핑.(자주 사용하지 않으니 매퍼를 따로 만들지않고 직접 조립)</p>
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void transactionalEventListenerAfterCommit(MyCdcApplicationEvent event) throws JsonProcessingException {
        myCdcProducer.sendMessage(
            MyModelConverter.toMessage(
                event.getId(),
                event.getMyModel(),
                event.getOperationType()
            )
        );
    }
}