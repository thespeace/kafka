package com.thespeace.kafkahandson.data;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thespeace.kafkahandson.model.MyModel;
import com.thespeace.kafkahandson.model.MyModelConverter;
import com.thespeace.kafkahandson.model.OperationType;
import com.thespeace.kafkahandson.producer.MyCdcProducer;
import jakarta.persistence.PostPersist;
import jakarta.persistence.PostRemove;
import jakarta.persistence.PostUpdate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MyEntityListener {

    @Autowired
    @Lazy
    private MyCdcProducer myCdcProducer;

    /**
     * <h2>@PostPersist</h2>
     * <p>DB의 Row가 Create되면, 해당 Annotation을 통해서 Listen할 수 있다.</p>
     */
    @PostPersist
    public void handleCreate(MyEntity myEntity) {
        System.out.println("handleCreate");
        MyModel myModel = MyModelConverter.toModel(myEntity);
        try {
            myCdcProducer.sendMessage(
                MyModelConverter.toMessage(
                    myEntity.getId(),
                    myModel,
                    OperationType.CREATE
                )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * <h2>@PostUpdate</h2>
     * <p>DB의 Row가 Update되면, 해당 Annotation을 통해서 Listen할 수 있다.</p>
     */
    @PostUpdate
    public void handleUpdate(MyEntity myEntity) {
        System.out.println("handleUpdate");
        MyModel myModel = MyModelConverter.toModel(myEntity);
        try {
            myCdcProducer.sendMessage(
                MyModelConverter.toMessage(
                    myEntity.getId(),
                    myModel,
                    OperationType.UPDATE
                )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @PostRemove
    public void handleDelete(MyEntity myEntity) {
        System.out.println("handleDelete");
        try {
            myCdcProducer.sendMessage(
                    MyModelConverter.toMessage(
                            myEntity.getId(),
                            null,
                            OperationType.DELETE
                    )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}