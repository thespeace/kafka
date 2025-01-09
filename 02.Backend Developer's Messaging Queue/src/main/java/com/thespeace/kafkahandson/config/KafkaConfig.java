package com.thespeace.kafkahandson.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration;

import java.util.HashMap;
import java.util.Map;

/**
 * <h1>KafkaTemplate 직접 구현</h1>
 * <p>기존의 방식은 Auto-Configuration 으로 인해서 만들어진 Bean을 통해 동작했는데, 직접 만든 Bean으로 동작시켜보자.</p>
 * <p>KafkaAutoConfiguration.java 를 살펴보면 @ConditionalMissingBean(KafkaTemplate.class)
 * KafkaTemplate Bean이 없다면 자동으로 생성해준다. 즉 백업용도라는 것인데, 보통은 Component scan 을 할 때,
 * Auto-Configuration 을 제외하고 작업을 해야하는데, Spring-Kafka는 그럴 필요가 없다.</p>
 * <p>만약 충돌이 생긴다면 @Primary로 대응하자.</p>
 *
 * @see KafkaAutoConfiguration
 * @see KafkaTemplate
 */
@Configuration
public class KafkaConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.kafka")
    public KafkaProperties kafkaProperties() {
        return new KafkaProperties();
    }

    @Bean
    @Primary
    public ConsumerFactory<String, Object> consumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getConsumer().getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getConsumer().getValueDeserializer());
        props.put(JsonDeserializer.TRUSTED_PACKAGES, "*");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false");
        props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false"); // 수동 커밋 설정 추가
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    @Primary
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
        ConsumerFactory<String, Object> consumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL); // 수동 커밋 설정 추가
        factory.setConcurrency(1); //선택사항
        return factory;
    }

    /**
     * <h2>Batch Listener</h2>
     * <p>지금까지 consume을 했을 때 메시지를 하나씩 읽었는데, 해당 옵션을 사용하면 한 번에 여러 개씩 읽을 수 있다.</p>
     * <p>옵션에 분기가 생긴 곳은 Consumer이기 때문에 관련 옵션 설정 파일만 새로 생성해주면 된다.</p>
     * <br><br>
     * <h2>실습 목표</h2>
     * <p>하나의 Topic이 두 개 이상의 Consumer group에 Consume.</p>
     */
    @Bean
    @Qualifier("batchConsumerFactory")
    public ConsumerFactory<String, Object> batchConsumerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getConsumer().getKeyDeserializer());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaProperties.getConsumer().getValueDeserializer());
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        props.put(ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG, "false");
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, ConsumerConfig.DEFAULT_MAX_POLL_RECORDS); // kafka로부터 한 번에 얼마나 records를 poll할지 max값 지정
        return new DefaultKafkaConsumerFactory<>(props);
    }

    @Bean
    @Qualifier("batchKafkaListenerContainerFactory")
    public ConcurrentKafkaListenerContainerFactory<String, Object> batchKafkaListenerContainerFactory(
            ConsumerFactory<String, Object> batchConsumerFactory
    ) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(batchConsumerFactory);
        factory.setBatchListener(true); // BatchListener 옵션 가동
        factory.setConcurrency(1);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.BATCH); // AckMode Batch로 변경
        return factory;
    }

    @Bean
    @Primary
    public ProducerFactory<String, Object> producerFactory(KafkaProperties kafkaProperties) {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaProperties.getBootstrapServers());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaProperties.getProducer().getKeySerializer());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaProperties.getProducer().getValueSerializer());
        props.put(ProducerConfig.ACKS_CONFIG, kafkaProperties.getProducer().getAcks());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true"); // EOS 적용
        return new DefaultKafkaProducerFactory<>(props);
    }

    @Bean
    @Primary
    public KafkaTemplate<String, ?> kafkaTemplate(KafkaProperties kafkaProperties) {
        return new KafkaTemplate<>(producerFactory(kafkaProperties));
    }

}
