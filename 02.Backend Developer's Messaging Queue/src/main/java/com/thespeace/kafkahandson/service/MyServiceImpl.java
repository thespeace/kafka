package com.thespeace.kafkahandson.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thespeace.kafkahandson.data.MyEntity;
import com.thespeace.kafkahandson.data.MyJpaRepository;
import com.thespeace.kafkahandson.model.MyModel;
import com.thespeace.kafkahandson.model.MyModelConverter;
import com.thespeace.kafkahandson.model.OperationType;
import com.thespeace.kafkahandson.producer.MyCdcProducer;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MyServiceImpl implements MyService {

    private final MyJpaRepository myJpaRepository;
    private final MyCdcProducer myCdcProducer;

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
     */
    @Override
    @Transactional
    public MyModel save(MyModel model) { // CREATE: model.getId() == null / UPDATE: model.getId() != null
        OperationType operationType = model.getId() == null ? OperationType.CREATE : OperationType.UPDATE;
        MyEntity entity = myJpaRepository.save(MyModelConverter.toEntity(model)); // If not exists, Create; else, Update

//        throw new RuntimeException("Error for sendMessage"); // @Transactional로 인해 Exception이 발생하면, DB Rollback이 이루어지기 때문에 어느정도 정합성이 보장.
        try {
            myCdcProducer.sendMessage(
                MyModelConverter.toMessage(
                    entity.getId(),
                    MyModelConverter.toModel(entity),
                    operationType // 분기 적용
                )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for sendMessage", e);
        }
        return MyModelConverter.toModel(entity);
    }

    @Override
    @Transactional
    public void delete(Integer id) { // D
        myJpaRepository.deleteById(id);

        // 애플리케이션단에서 데이터 변경 Produce를 하려면 필연적으로 DB에 먼저 저장을 하고, 미리 정의해둔 메서드를 통해서 Produce 해야 한다.
        try {
            myCdcProducer.sendMessage(
                MyModelConverter.toMessage(
                    id,
                    null, // CREATE: Data X -> Data O / UPDATE: Data O -> Data X
                    OperationType.DELETE
                )
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error processing JSON for sendMessage", e);
        }
    }
}