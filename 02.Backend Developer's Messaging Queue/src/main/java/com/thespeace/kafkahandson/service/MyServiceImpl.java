package com.thespeace.kafkahandson.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thespeace.kafkahandson.data.MyEntity;
import com.thespeace.kafkahandson.data.MyJpaRepository;
import com.thespeace.kafkahandson.event.MyCdcApplicationEvent;
import com.thespeace.kafkahandson.model.MyModel;
import com.thespeace.kafkahandson.model.MyModelConverter;
import com.thespeace.kafkahandson.model.OperationType;
import com.thespeace.kafkahandson.producer.MyCdcProducer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MyServiceImpl implements MyService {

    private final MyJpaRepository myJpaRepository;
    private final MyCdcProducer myCdcProducer;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * MyEntity -> MyModel
     */
    @Override
    public List<MyModel> findAll() {
        List<MyEntity> entities = myJpaRepository.findAll();
        return entities.stream().map(MyModelConverter::toModel).toList();
    }

    /**
     * MyEntity -> MyModel
     */
    @Override
    public MyModel findById(Integer id) {
        Optional<MyEntity> entity = myJpaRepository.findById(id);
        return entity.map(MyModelConverter::toModel).orElse(null);
    }

    /**
     * MyEntity -> MyModel
     *
     * <h2>@Transactional</h2>
     * <p>애플리케이션단에서 CDC 니즈를 충족하려면 결국에는 Dual Write 개념이 적용이 되는데, 어느정도 원자성을 갖게 하기 위해 해당 annotation이 필요하다.</p>
     * <p>하지만 완벽한 원자성을 갖기는 어려운데, 만약 제일 마지막에 예외가 발생한다면 DB에서는 Rollback이 적용되지만 Produce는 Rollback이 되지 않는다.</p>
     */
    @Override
    @Transactional
    public MyModel save(MyModel model) { // CREATE: model.getId() == null / UPDATE: model.getId() != null
        OperationType operationType = model.getId() == null ? OperationType.CREATE : OperationType.UPDATE;
        MyEntity entity = myJpaRepository.save(MyModelConverter.toEntity(model)); // If not exists, Create; else, Update
        MyModel resultModel = MyModelConverter.toModel(entity);

        //내부 이벤트 발행
        applicationEventPublisher.publishEvent(
            new MyCdcApplicationEvent(
                this,
                entity.getId(),
                resultModel,
                operationType
            )
        );
        return resultModel;
    }

    @Override
    @Transactional
    public void delete(Integer id) { // D
        myJpaRepository.deleteById(id);
        applicationEventPublisher.publishEvent(
            new MyCdcApplicationEvent(
                this,
                id,
                null,
                OperationType.DELETE
            )
        );
    }
}