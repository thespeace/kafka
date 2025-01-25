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
     * <p>하지만 앞의 트랜잭션이 커밋되고 나서 해당 메서드에서 예외가 발생한다면, DB Rollback이 실행되지 않아 데이터 정합성에 문제가 발생할 수 있다.
     * 따라서 더 개선된 방법으로 Entity event listener을 활용하여 CDC를 구현할 수 있다.</p>
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