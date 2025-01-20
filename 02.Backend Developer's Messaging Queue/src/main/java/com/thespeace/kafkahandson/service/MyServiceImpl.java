package com.thespeace.kafkahandson.service;

import com.thespeace.kafkahandson.data.MyEntity;
import com.thespeace.kafkahandson.data.MyJpaRepository;
import com.thespeace.kafkahandson.model.MyModel;
import com.thespeace.kafkahandson.model.MyModelConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class MyServiceImpl implements MyService {

    private final MyJpaRepository myJpaRepository;

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
     */
    @Override
    public MyModel save(MyModel model) {
        MyEntity entity = myJpaRepository.save(MyModelConverter.toEntity(model));
        return MyModelConverter.toModel(entity);
    }

    @Override
    public void delete(Integer id) {
        myJpaRepository.deleteById(id);
    }
}