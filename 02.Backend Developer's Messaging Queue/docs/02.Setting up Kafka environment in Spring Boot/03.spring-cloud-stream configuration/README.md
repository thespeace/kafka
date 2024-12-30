# spring-cloud-stream 설정

<br>

## spring-cloud-stream 구조

![spring-cloud-stream structure](../../../md_resource/spring-cloud-stream%20structure.PNG)

Spring Application이 Kafka, RabbitMQ와 같은 외부 메시징 큐와 직접 교류하는 것이 아니라 둘 사이에 존재하는 `Binder`와 `Binding`이라는 개념을 통해서 교류한다. 
* `Binder`
  * spring-cloud-stream이 제공해주는 미들 웨어(Kafka)와의 `통신 컴포넌트`로 보면 된다.
  * spring이 설정정보를 읽어서 미들웨어 Binder를 구현체로 제공해준다. 따라서 애플리케이션을 개발할 때에는 Kafka라고 하는 미들웨어와 엮이지않고 독립적으로 개발이 가능한 것이다.  
    다시말해서 Kafka라고 하는 특정 메시징 시스템에 의존하지 않고, 추상화된 방식으로 메시징 큐를 이용할 수 있다.
  * 그래서 Kafka를 사용하다가 모종의 이유로 인해 RabbitMQ로 변경해야 하는 경우가 생겨도 코드의 변경이 거의 없이 메시징 큐 시스템을 변경할 수 있는 것이다.  
    즉, 구현체가 아니라 추상화된 영역에 의존을 하기 때문에 더 유연해지는 사례이다.
  * broker와 비교를 해보자면, 대략 비슷한 개념인데 정확히는 broker 설정을 추상화시킨 개념이고 broker 자체는 아니다.
* `Binding`
  * 외부 메시징 시스템과 애플리케이션간의 브릿지 역할을 한다.
  * 한마디로 Consumer, Producer 의 설정을 추상화 시킨 개념이라고 이해하면 된다.
  * spring-cloud-stream에서는 프로세서, 소스, 싱크라는 바인딩들이 인터페이스로 제공이 된다.

<br>

### 지금까지 개념적인 것들을 살펴보았다면, spring-cloud-stream을 실제로 어떻게 설정하는지 살펴보자.

![spring-cloud-stream structure](../../../md_resource/spring-cloud-stream%20structure2.PNG)

크게 세가지 영역이 가장 핵심적인 요소이자 가장 헷갈리는 부분이다.  
조금 쉽게 이야기해보자면 broker관련 설정은 binder를 살펴보면 되고, consumer,producer 관련 설정은 bindings을 살펴보면 된다.

* spring.cloud.stream.`kafka.binder`
  * binder 공통 설정
* spring.cloud.stream.`kafka.bindings`
  * output/input 채널 에 대한 카프카 특화 설정
* spring.cloud.stream.`bindings`
  * output/input 채널에 대한 공통 설정
  * destination: kafka에서는 target이 되는 queue를 topic이라고 부르는데, bindings는 조금 더 추상화시켜서 destination이라는 용어를 사용한다.

<br>

![spring-cloud-stream structure](../../../md_resource/spring-cloud-stream%20structure3.PNG)

* spring.cloud.`function.definition`
  * bean 정의
* spring.cloud.`stream.function.bindings`
  * bean과 채널을 바인딩 해주는 역할
    * 채널: bingdings에 정의
  * 해당 부분을 작성할때 네이밍 컨벤션을 준수해야 한다.
    * `bean 이름`-`out 또는 in`-`index`
      * `out 또는 in` : `out`은 produce를 의미하고, 반대로 `in`은 consume을 뜻한다.
      * `index` : 보통은 0으로 작성하지만 1이나 2를 작성하는 경우는 consumer가 하나의 topic만 consume한게 아니라 두 개 이상의 topic을 consume하게 된다면 여러 채널을 지정해야 하기 때문에 마지막에 숫자를 붙여서 구분하게 된다. 

<br>

## 실습

* `build.gradle`에서 spring-cloud-stream과 관련된 2가지 의존성을 주입
  ```groovy
  implementation 'org.springframework.cloud:spring-cloud-stream:4.0.3'
  implementation 'org.springframework.cloud:spring-cloud-stream-binder-kafka:4.0.3'
  ```

<br>

* `src/main/resources/application.yml` 생성
  * spring-cloud-stream은 설정 기반으로 동작하기 때문에 해당 파일 작성이 가장 중요하다.
  ```yaml
  spring:
  cloud:
    function:
      definition: myProducer;myConsumer; #외부에서 사용할 bean 이름 지정
    stream:
      function:
        bindings:
          myProducer-out-0: producer-test #bean과 채널을 바인딩
          myConsumer-in-0: consumer-test  #bean과 채널을 바인딩
      kafka: #kafka 관련 설정
        binder:
          brokers: localhost:9092,localhost:9093,localhost:9094
          auto-create-topics: false
          required-acks: 0
          configuration:
            key.serializer: org.apache.kafka.common.serialization.StringSerializer
        bindings:
          consumer-test:
            consumer:
              start-offset: latest
      bindings:
        producer-test: #producer 채널 설정
          destination: my-json-topic
          content-type: application/json
        consumer-test: #consumer 채널 설정
          destination: my-json-topic
          group: test-consumer-group
          consumer: #consumer 설정
            concurrency: 1
  ```
  * 이제 spring-cloud-stream을 사용할 수 있는 최소한의 설정을 완료하였다.

<br>

* 본격적으로 spring-cloud-stream를 사용해보자.
  * producer와 consumer를 구현에 앞서 사용할 메시지의 스키마를 작성.
    * `src/main/java/com/thespeace/kafkahandson/model/MyMessage.java`
  * consumer와 producer 구현.
    * `src/main/java/com/thespeace/kafkahandson/consumer/MyConsumer.java`
    * `src/main/java/com/thespeace/kafkahandson/producer/MyProducer.java`
  * 메시지를 발행할 트리거 작성
    * `src/main/java/com/thespeace/kafkahandson/api/MyController.java`