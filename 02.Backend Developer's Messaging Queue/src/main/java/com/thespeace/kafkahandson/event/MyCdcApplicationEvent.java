package com.thespeace.kafkahandson.event;

import com.thespeace.kafkahandson.model.MyModel;
import com.thespeace.kafkahandson.model.OperationType;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * <h1>Application event</h1>
 * <p>Kafka event와는 이벤트라는 컨셉은 같지만, 전혀 다른 개념이다.</p>
 */
@Getter
public class MyCdcApplicationEvent extends ApplicationEvent {

    private final Integer id;
    private final MyModel myModel;
    private final OperationType operationType;

    public MyCdcApplicationEvent(Object source, Integer id, MyModel myModel, OperationType operationType) {
        super(source);
        this.id = id;
        this.myModel = myModel;
        this.operationType = operationType;
    }
}