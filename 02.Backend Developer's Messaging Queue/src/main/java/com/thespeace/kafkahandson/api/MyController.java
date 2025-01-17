package com.thespeace.kafkahandson.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.thespeace.kafkahandson.model.MyMessage;
import com.thespeace.kafkahandson.producer.MyProducer;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * @link <a href="http://localhost:8080/swagger-ui/index.html">swagger-ui</a>
 */
@RequiredArgsConstructor
@RestController
public class MyController {

    private final MyProducer myProducer;

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

}
