package com.thespeace.kafkahandson.consumer;

import com.thespeace.kafkahandson.model.MyMessage;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component
public class MyConsumer implements Consumer<Message<MyMessage>> {

    @Override
    public void accept(Message<MyMessage> message) {
        System.out.println("Message arrived! - " + message.getPayload());
    }
}
