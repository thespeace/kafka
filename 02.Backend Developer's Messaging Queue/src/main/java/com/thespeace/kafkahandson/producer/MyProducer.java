package com.thespeace.kafkahandson.producer;


import com.thespeace.kafkahandson.model.MyMessage;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.function.Supplier;

/**
 * <p>만일 Flux를 사용하지 않고 Supplier만 사용한다면, 기본적인 설정으로 인해 1초마다 하나씩 메시지를 생산하게 되어 있다.
 * 메시지를 발행하고 싶을때만 payLoad에 발행하기 위해 Flux를 사용해줘야 한다.</p>
 * <br>
 * <p>만약에 위의 작업이 번거롭다면 StreamBridge를 사용해서 topic명을 지정해서 간단하게 메시지를 보낼 수 있는 기능이 있다.
 * 하지만 spring-cloud-stream의 개념적으로 조금 거리감이 있어서 해당 방법보다는 위의 형태를 가지는게 좋다.</p>
 */
@Component
public class MyProducer implements Supplier<Flux<Message<MyMessage>>> {

    MyProducer() {
        System.out.println("MyProducer init!");
    }

    private final Sinks.Many<Message<MyMessage>> sinks = Sinks.many().unicast().onBackpressureBuffer();

    public void sendMessage(MyMessage myMessage) {
        Message<MyMessage> message = MessageBuilder
                .withPayload(myMessage)
                .setHeader(KafkaHeaders.KEY, String.valueOf(myMessage.getAge())) //나이를 메시지 key로 사용
                .build();
        sinks.emitNext(message, Sinks.EmitFailureHandler.FAIL_FAST);
    }

    @Override
    public Flux<Message<MyMessage>> get() {
        return sinks.asFlux();
    }
}
