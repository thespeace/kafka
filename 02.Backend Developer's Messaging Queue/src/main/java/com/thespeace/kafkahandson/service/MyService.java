package com.thespeace.kafkahandson.service;

import com.thespeace.kafkahandson.model.MyModel;

import java.util.List;

public interface MyService {

    public List<MyModel> findAll();
    public MyModel findById(Integer id);
    public MyModel save(MyModel model);
    public void delete(Integer id); // 실습 편의상 soft delete X, hard delete O
}