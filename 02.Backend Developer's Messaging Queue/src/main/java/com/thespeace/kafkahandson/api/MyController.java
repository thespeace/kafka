package com.thespeace.kafkahandson.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thespeace.kafkahandson.model.MyMessage;
import com.thespeace.kafkahandson.model.MyModel;
import com.thespeace.kafkahandson.producer.MyProducer;
import com.thespeace.kafkahandson.service.MyService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @link <a href="http://localhost:8080/swagger-ui/index.html">swagger-ui</a>
 */
@RequiredArgsConstructor
@RestController
public class MyController {

    private final MyProducer myProducer;
    private final MyService myService;

    @RequestMapping("/hello")
    String hello() {
        return "Hello World";
    }

    /**
     * <h2>메시지를 발행할 트리거</h2>
     */
    @PostMapping("/message")
    void message(
        @RequestBody MyMessage message
    ) {
        try {
            myProducer.sendMessage(message);
        } catch (JsonProcessingException e) {
            e.fillInStackTrace();
        }
    }

    /**
     * <h2>CDC 실습 - CREATE</h2>
     * <p>CRUD에서 HttpStatus를 위한 상세한 핸들링은 생략</p>
     */
    @PostMapping("/greetings")
    MyModel create(
            @RequestBody Request request
    ) {
        if (
                request == null ||
                        request.userId == null ||
                        request.userName == null ||
                        request.userAge == null ||
                        request.content == null
        ) return null;

        MyModel myModel = MyModel.create(
                request.userId,
                request.userAge,
                request.userName,
                request.content
        );
        return myService.save(myModel);
    }

    /**
     * <h2>CDC 실습 - READ</h2>
     */
    @GetMapping("/greetings")
    List<MyModel> list() {
        return myService.findAll();
    }

    @GetMapping("/greetings/{id}")
    MyModel get(
            @PathVariable Integer id
    ) {
        return myService.findById(id);
    }

    /**
     * <h2>CDC 실습 - UPDATE</h2>
     */
    @PatchMapping("/greetings/{id}")
    MyModel update(
            @PathVariable Integer id,
            @RequestBody String content
    ) {
        if (id == null || content == null || content.isBlank()) return null;
        MyModel myModel = myService.findById(id);
        myModel.setContent(content);
        return myService.save(myModel);
    }

    /**
     * <h2>CDC 실습 - DELETE</h2>
     */
    @DeleteMapping("/greetings/{id}")
    void delete(
            @PathVariable Integer id
    ) {
        myService.delete(id);
    }

    /**
     * <h2>CDC 실습 - 보일러플레이트</h2>
     */
    @Data
    private static class Request {
        Integer userId;
        String userName;
        Integer userAge;
        String content;
    }
}